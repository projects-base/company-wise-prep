package com.companywiseprep.code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.data.Yaml;
import com.companywiseprep.progress.ProgressService;
import com.companywiseprep.web.NotFoundException;

/**
 * LeetCode-style judging: the learner's Solution.java is compiled with the hidden harness and
 * the shared IO library, then every test runs in its own JVM and stdout is compared exactly
 * (after trimming trailing whitespace), as docs/CODE-CHALLENGES.md specifies.
 */
@Service
public class JudgeService {

	/** Truncation for test text echoed back to the browser. */
	static final int ECHO_LIMIT = 4_000;
	static final long TEST_TIMEOUT_MS = 6_000;
	static final long PLAYGROUND_TIMEOUT_MS = 10_000;
	/** Playground output shown in the browser. */
	static final int PLAYGROUND_ECHO_LIMIT = 64 * 1024;
	private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+(?:final\\s+)?class\\s+(\\w+)");

	private final CodeChallengeRepository challenges;
	private final JavaRunner runner;
	private final ProgressService progress;
	private final java.nio.file.Path ioLibrary;
	/** A single user can still click Run twice; keep the machine responsive. */
	private final Semaphore slots = new Semaphore(2);
	private final ExecutorService pool = Executors.newFixedThreadPool(Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2)));

	public JudgeService(CodeChallengeRepository challenges, JavaRunner runner, ProgressService progress,
			@Value("${prep.data-dir}") java.nio.file.Path dataDir) {
		this.challenges = challenges;
		this.runner = runner;
		this.progress = progress;
		this.ioLibrary = dataDir.resolve("code").resolve("_lib").resolve("IO.java");
	}

	public enum Mode {
		/** Visible examples only, every result shown. */
		RUN,
		/** Every test; hidden inputs are revealed only for the first failure. */
		SUBMIT
	}

	public enum Verdict {
		ACCEPTED, WRONG_ANSWER, RUNTIME_ERROR, TIME_LIMIT, COMPILE_ERROR
	}

	public record TestCase(String name, String input, String expected, boolean hidden) {
	}

	public record ChallengeView(String slug, String method, String problem, String starter, String savedCode,
			List<TestCase> examples, int hiddenCount) {
	}

	public record TestResult(String name, boolean hidden, Verdict verdict, String input, String expected,
			String actual, String stderr, long millis) {
	}

	public record JudgeResult(Verdict verdict, int passed, int total, long maxMillis,
			List<JavaRunner.CompileError> compileErrors, List<TestResult> results) {
	}

	public record PlaygroundResult(Verdict verdict, String stdout, String stderr, long millis, boolean truncated,
			List<JavaRunner.CompileError> compileErrors) {
	}

	@Transactional(readOnly = true)
	public ChallengeView view(String slug) {
		CodeChallenge c = get(slug);
		List<TestCase> tests = tests(c);
		return new ChallengeView(c.getSlug(), c.getMethod(), c.getProblem(), c.getStarter(),
				progress.of(slug).code(),
				tests.stream().filter(t -> !t.hidden()).toList(),
				(int) tests.stream().filter(TestCase::hidden).count());
	}

	@Transactional(readOnly = true)
	public String reference(String slug) {
		return get(slug).getReference();
	}

	/**
	 * @param customInput when present (RUN only), runs just that input; the expected output comes
	 *                    from running the reference solution on it
	 */
	public JudgeResult judge(String slug, String code, Mode mode, String customInput) {
		CodeChallenge c = get(slug);
		List<TestCase> tests = tests(c);
		boolean custom = mode == Mode.RUN && customInput != null && !customInput.isBlank();
		if (custom) {
			tests = List.of(new TestCase("Custom input", customInput, expectedFromReference(c, customInput), false));
		} else if (mode == Mode.RUN) {
			tests = tests.stream().filter(t -> !t.hidden()).toList();
		}
		List<TestCase> selected = tests;
		return withSlot(() -> {
			try (JavaRunner.Compiled compiled = runner.compile(sources(c.getHarness(), code))) {
				if (!compiled.ok()) {
					return new JudgeResult(Verdict.COMPILE_ERROR, 0, 0, 0, compiled.errors(), List.of());
				}
				List<TestResult> results = runAll(compiled, selected);
				return summarise(results, mode);
			}
		});
	}

	public PlaygroundResult playground(String code, String stdin) {
		Matcher m = PUBLIC_CLASS.matcher(code);
		String mainClass = m.find() ? m.group(1) : "Main";
		return withSlot(() -> {
			try (JavaRunner.Compiled compiled = runner.compile(Map.of(mainClass + ".java", code))) {
				if (!compiled.ok()) {
					return new PlaygroundResult(Verdict.COMPILE_ERROR, "", "", 0, false, compiled.errors());
				}
				JavaRunner.Execution e = runner.run(compiled, mainClass, stdin, PLAYGROUND_TIMEOUT_MS);
				Verdict v = switch (e.outcome()) {
					case OK -> Verdict.ACCEPTED;
					case TIME_LIMIT -> Verdict.TIME_LIMIT;
					case RUNTIME_ERROR -> Verdict.RUNTIME_ERROR;
				};
				boolean cut = e.truncated() || e.stdout().length() > PLAYGROUND_ECHO_LIMIT;
				String out = e.stdout().length() > PLAYGROUND_ECHO_LIMIT ? e.stdout().substring(0, PLAYGROUND_ECHO_LIMIT) : e.stdout();
				String err = e.stderr().length() > PLAYGROUND_ECHO_LIMIT ? e.stderr().substring(0, PLAYGROUND_ECHO_LIMIT) : e.stderr();
				return new PlaygroundResult(v, out, err, e.millis(), cut, List.of());
			}
		});
	}

	private List<TestResult> runAll(JavaRunner.Compiled compiled, List<TestCase> tests) {
		List<Future<TestResult>> futures = new ArrayList<>();
		for (TestCase t : tests) {
			futures.add(pool.submit(() -> {
				JavaRunner.Execution e = runner.run(compiled, "Main", t.input(), TEST_TIMEOUT_MS);
				Verdict v = switch (e.outcome()) {
					case TIME_LIMIT -> Verdict.TIME_LIMIT;
					case RUNTIME_ERROR -> Verdict.RUNTIME_ERROR;
					case OK -> normalise(e.stdout()).equals(normalise(t.expected())) ? Verdict.ACCEPTED : Verdict.WRONG_ANSWER;
				};
				return new TestResult(t.name(), t.hidden(), v, t.input(), t.expected(), e.stdout(), e.stderr(), e.millis());
			}));
		}
		List<TestResult> out = new ArrayList<>();
		for (Future<TestResult> f : futures) {
			try {
				out.add(f.get());
			} catch (Exception e) {
				throw new IllegalStateException("Test execution failed", e);
			}
		}
		return out;
	}

	private JudgeResult summarise(List<TestResult> results, Mode mode) {
		int passed = (int) results.stream().filter(r -> r.verdict() == Verdict.ACCEPTED).count();
		Verdict overall = results.stream().map(TestResult::verdict).filter(v -> v != Verdict.ACCEPTED).findFirst()
				.orElse(Verdict.ACCEPTED);
		long max = results.stream().mapToLong(TestResult::millis).max().orElse(0);
		boolean revealed = false;
		List<TestResult> shown = new ArrayList<>();
		for (TestResult r : results) {
			boolean reveal = !r.hidden() || mode == Mode.RUN
					|| (r.verdict() != Verdict.ACCEPTED && !revealed);
			if (r.hidden() && reveal) revealed = true;
			shown.add(reveal
					? new TestResult(r.name(), r.hidden(), r.verdict(), clip(r.input()), clip(r.expected()), clip(r.actual()),
							clip(r.stderr()), r.millis())
					: new TestResult(r.name(), true, r.verdict(), null, null, null, null, r.millis()));
		}
		return new JudgeResult(overall, passed, results.size(), max, List.of(), shown);
	}

	private String expectedFromReference(CodeChallenge c, String input) {
		try (JavaRunner.Compiled ref = runner.compile(sources(c.getHarness(), c.getReference()))) {
			if (!ref.ok()) return "";
			JavaRunner.Execution e = runner.run(ref, "Main", input, TEST_TIMEOUT_MS);
			return e.outcome() == JavaRunner.Outcome.OK ? e.stdout()
					: "(the reference solution could not run this input: " + firstLine(e.stderr()) + ")";
		}
	}

	private Map<String, String> sources(String harness, String solution) {
		Map<String, String> m = new LinkedHashMap<>();
		try {
			m.put("IO.java", java.nio.file.Files.readString(ioLibrary));
		} catch (java.io.IOException e) {
			throw new IllegalStateException("Missing " + ioLibrary, e);
		}
		m.put("Main.java", harness);
		m.put("Solution.java", solution);
		return m;
	}

	private CodeChallenge get(String slug) {
		return challenges.findById(slug).orElseThrow(() -> new NotFoundException("No code challenge for " + slug));
	}

	static List<TestCase> tests(CodeChallenge c) {
		List<TestCase> out = new ArrayList<>();
		int i = 1;
		for (Map<String, Object> t : Yaml.maps(Yaml.load(c.getTests()).get("tests"))) {
			out.add(new TestCase(
					t.get("name") == null ? "Test " + i : String.valueOf(t.get("name")),
					raw(t.get("input")), raw(t.get("expected")),
					Boolean.TRUE.equals(t.get("hidden"))));
			i++;
		}
		return out;
	}

	/** YAML block scalars keep their text; don't trim the inside of inputs. */
	private static String raw(Object o) {
		return o == null ? "" : String.valueOf(o);
	}

	static String normalise(String s) {
		if (s == null) return "";
		String[] lines = s.replace("\r\n", "\n").split("\n", -1);
		StringBuilder sb = new StringBuilder();
		for (String l : lines) sb.append(l.stripTrailing()).append('\n');
		return sb.toString().strip();
	}

	private static String clip(String s) {
		if (s == null) return null;
		return s.length() <= ECHO_LIMIT ? s : s.substring(0, ECHO_LIMIT) + "\n… (" + (s.length() - ECHO_LIMIT) + " more characters)";
	}

	private static String firstLine(String s) {
		return s == null ? "" : s.strip().lines().findFirst().orElse("");
	}

	private <T> T withSlot(java.util.function.Supplier<T> work) {
		try {
			slots.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted waiting to run code", e);
		}
		try {
			return work.get();
		} finally {
			slots.release();
		}
	}
}
