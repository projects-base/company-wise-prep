package com.companywiseprep.code;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Component;

/**
 * Compiles Java sources in-process and runs them in a separate, limited JVM.
 *
 * Isolation is a time limit, a heap limit and a throwaway directory — enough for a single
 * user's own practice code on their own machine, not for untrusted code from others.
 */
@Component
public class JavaRunner {

	/**
	 * Output read back for comparison. Must exceed the largest expected output (some hidden tests
	 * print ~100k numbers); what is *displayed* is clipped separately by the caller.
	 */
	static final int OUTPUT_LIMIT = 16 * 1024 * 1024;

	/** Heap for each test JVM (prep.judge.heap). Several can run at once, so size it to the host. */
	private final String heap;

	public JavaRunner(@org.springframework.beans.factory.annotation.Value("${prep.judge.heap:512m}") String heap) {
		this.heap = heap;
	}

	/**
	 * The learner's code must not see the server's secrets (DB_PASSWORD, APP_PASSWORD, ...), so the
	 * child JVM gets only what the OS needs to start a process.
	 */
	static void restrictEnvironment(Map<String, String> env) {
		Map<String, String> keep = new java.util.HashMap<>();
		for (String k : List.of("PATH", "SystemRoot", "SYSTEMROOT", "TEMP", "TMP", "TMPDIR", "HOME", "LANG")) {
			if (env.containsKey(k)) keep.put(k, env.get(k));
		}
		env.clear();
		env.putAll(keep);
	}

	private final String javaBin = Path.of(System.getProperty("java.home"), "bin",
			System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java").toString();

	public record CompileError(String file, long line, long column, String message) {
	}

	public record Compiled(Path dir, List<CompileError> errors) implements AutoCloseable {

		public boolean ok() {
			return errors.isEmpty();
		}

		@Override
		public void close() {
			deleteQuietly(dir);
		}
	}

	public enum Outcome {
		OK, RUNTIME_ERROR, TIME_LIMIT
	}

	public record Execution(Outcome outcome, String stdout, String stderr, long millis, boolean truncated) {
	}

	/** Compiles the given file-name → source map into a fresh temp directory. */
	public Compiled compile(Map<String, String> sources) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) throw new IllegalStateException("No Java compiler — the app must run on a JDK, not a JRE");
		Path dir;
		try {
			dir = Files.createTempDirectory("cwp-run-");
			List<Path> files = new ArrayList<>();
			for (var e : sources.entrySet()) {
				Path f = dir.resolve(e.getKey());
				Files.writeString(f, e.getValue(), StandardCharsets.UTF_8);
				files.add(f);
			}
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
			try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
				boolean ok = compiler.getTask(null, fm, diagnostics,
						List.of("-d", dir.toString(), "-encoding", "UTF-8", "-nowarn", "-proc:none"), null,
						fm.getJavaFileObjectsFromPaths(files)).call();
				List<CompileError> errors = new ArrayList<>();
				for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
					if (d.getKind() != Diagnostic.Kind.ERROR) continue;
					String file = d.getSource() == null ? "" : Path.of(d.getSource().toUri()).getFileName().toString();
					errors.add(new CompileError(file, d.getLineNumber(), d.getColumnNumber(), d.getMessage(Locale.ROOT)));
				}
				if (!ok && errors.isEmpty()) errors.add(new CompileError("", 0, 0, "Compilation failed"));
				return new Compiled(dir, errors);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Runs {@code mainClass} from a compiled directory with the given stdin. */
	public Execution run(Compiled compiled, String mainClass, String stdin, long timeoutMillis) {
		Path base = compiled.dir();
		try {
			Path work = Files.createTempDirectory(base, "exec-");
			Path in = work.resolve("in.txt");
			Path out = work.resolve("out.txt");
			Path err = work.resolve("err.txt");
			Files.writeString(in, stdin == null ? "" : stdin, StandardCharsets.UTF_8);
			ProcessBuilder pb = new ProcessBuilder(javaBin, "-Xmx" + heap, "-Xss64m", "-XX:+UseSerialGC",
					"-Xshare:auto", "-Dfile.encoding=UTF-8",
					"-Dstdout.encoding=UTF-8", "-cp", base.toString(), mainClass)
					.directory(work.toFile())
					.redirectInput(in.toFile())
					.redirectOutput(out.toFile())
					.redirectError(err.toFile());
			restrictEnvironment(pb.environment());
			long start = System.nanoTime();
			Process p = pb.start();
			boolean finished = p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
			long millis = (System.nanoTime() - start) / 1_000_000;
			if (!finished) {
				p.descendants().forEach(ProcessHandle::destroyForcibly);
				p.destroyForcibly();
				p.waitFor(2, TimeUnit.SECONDS);
			}
			Read o = read(out);
			Read e = read(err);
			Outcome outcome = !finished ? Outcome.TIME_LIMIT : p.exitValue() == 0 ? Outcome.OK : Outcome.RUNTIME_ERROR;
			return new Execution(outcome, o.text(), e.text(), millis, o.truncated() || e.truncated());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while running code", e);
		}
	}

	private record Read(String text, boolean truncated) {
	}

	private static Read read(Path f) throws IOException {
		long size = Files.size(f);
		byte[] bytes;
		try (var s = Files.newInputStream(f)) {
			bytes = s.readNBytes((int) Math.min(size, OUTPUT_LIMIT));
		}
		return new Read(new String(bytes, StandardCharsets.UTF_8), size > OUTPUT_LIMIT);
	}

	static void deleteQuietly(Path dir) {
		if (dir == null || !Files.exists(dir)) return;
		try (Stream<Path> walk = Files.walk(dir)) {
			walk.sorted(Comparator.reverseOrder()).forEach(p -> {
				try {
					Files.deleteIfExists(p);
				} catch (IOException ignored) {
					// a file still held open by a killed process; the OS temp cleaner gets it later
				}
			});
		} catch (IOException ignored) {
			// best effort
		}
	}
}
