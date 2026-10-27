package com.companywiseprep.frequency;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.companywiseprep.bank.BankService;
import com.companywiseprep.bank.BankService.QuestionSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * How often LeetCode tags each problem for a company, read live from the community repo
 * liquidslr/leetcode-company-wise-problems (built from LeetCode's company tags). Nothing from it is
 * stored in this repo: lists are fetched on demand and cached in memory. A frequency is a signal
 * for ordering practice, not a sighting: it never says a problem was asked in a real interview.
 */
@Service
public class FrequencyService {

	public static final String REPO = "liquidslr/leetcode-company-wise-problems";
	private static final String RAW = "https://raw.githubusercontent.com/" + REPO + "/main/";
	private static final String API = "https://api.github.com/repos/" + REPO + "/contents/";
	private static final Duration LIST_TTL = Duration.ofHours(6);
	private static final Duration FOLDERS_TTL = Duration.ofHours(24);

	/** The repo's time windows, by the file each one lives in. */
	public enum Window {
		THIRTY_DAYS("1. Thirty Days"), THREE_MONTHS("2. Three Months"), SIX_MONTHS("3. Six Months"),
		OLDER("4. More Than Six Months"), ALL("5. All");

		final String file;

		Window(String file) {
			this.file = file;
		}

		public static Window parse(String s) {
			return s == null || s.isBlank() ? THREE_MONTHS : valueOf(s.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
		}
	}

	public record Row(String title, String leetcode, String difficulty, double frequency, double acceptance,
			List<String> topics, String link, String question, boolean done) {
	}

	public record ListView(String folder, String window, String source, Instant fetchedAt, List<Row> rows,
			int inBank, int done) {
	}

	/** One company that tags a problem, with its frequency score (0-100). */
	public record Tag(String company, double frequency) {
	}

	private record Cached<T>(T value, Instant at) {
	}

	private final BankService bank;
	private final Clock clock;
	private final List<String> hotCompanies;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL).build();
	private final ObjectMapper json = new ObjectMapper();
	private final Map<String, Cached<List<RawRow>>> lists = new ConcurrentHashMap<>();
	private volatile Cached<List<String>> folders;
	private volatile Cached<Map<String, List<Tag>>> hot;

	public FrequencyService(BankService bank, Clock clock,
			@Value("${prep.frequency.hot-companies:Google,Amazon,Microsoft,Meta,Uber,Atlassian,Flipkart,Walmart Labs,Oracle,Salesforce,Adobe,Goldman Sachs,PayPal,PhonePe,Swiggy}") String hotCompanies) {
		this.bank = bank;
		this.clock = clock;
		this.hotCompanies = Arrays.stream(hotCompanies.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}

	record RawRow(String title, String leetcode, String difficulty, double frequency, double acceptance,
			List<String> topics, String link) {
	}

	/** Every company folder in the repo, for the picker. */
	public List<String> folders() {
		Cached<List<String>> c = folders;
		if (c != null && fresh(c.at(), FOLDERS_TTL)) return c.value();
		JsonNode root = getJson(API);
		List<String> out = new ArrayList<>();
		for (JsonNode n : root) {
			if ("dir".equals(n.path("type").asText())) out.add(n.path("name").asText());
		}
		out.sort(String.CASE_INSENSITIVE_ORDER);
		folders = new Cached<>(List.copyOf(out), Instant.now(clock));
		return folders.value();
	}

	/**
	 * The folder for one of our companies: its {@code leetcode_company} in company.yaml, else the
	 * folder whose name matches the company name or an alias. Empty when the repo doesn't cover it.
	 */
	public Optional<String> folderFor(String companySlug) {
		BankService.CompanyView c = bank.company(companySlug);
		Object explicit = c.data().get("leetcode_company");
		if (explicit != null) return Optional.of(explicit.toString());
		List<String> names = new ArrayList<>();
		names.add(c.name());
		if (c.data().get("aliases") instanceof List<?> aliases) aliases.forEach(a -> names.add(String.valueOf(a)));
		List<String> all = folders();
		for (String n : names) {
			for (String f : all) {
				if (f.equalsIgnoreCase(n.trim())) return Optional.of(f);
			}
		}
		return Optional.empty();
	}

	/** One company list, with each problem matched to the bank and to your progress. */
	public ListView list(String folder, Window window) {
		Map<String, QuestionSummary> byLeetcode = bank.questions().stream()
				.filter(q -> q.leetcode() != null && !q.leetcode().isBlank())
				.collect(Collectors.toMap(QuestionSummary::leetcode, Function.identity(), (a, b) -> a));
		String key = folder + "/" + window.file;
		List<Row> rows = raw(folder, window).stream().map(r -> {
			QuestionSummary q = byLeetcode.get(r.leetcode());
			return new Row(r.title(), r.leetcode(), r.difficulty(), r.frequency(), r.acceptance(), r.topics(), r.link(),
					q == null ? null : q.slug(), q != null && q.done());
		}).toList();
		return new ListView(folder, window.name().toLowerCase(Locale.ROOT).replace('_', '-'), sourceUrl(folder, window),
				lists.get(key).at(), rows, (int) rows.stream().filter(r -> r.question() != null).count(),
				(int) rows.stream().filter(Row::done).count());
	}

	/**
	 * For every bank problem: which of the big product companies tagged it in the last six months,
	 * most frequent first. Used for the "hot" badges on the plan and question pages.
	 */
	public Map<String, List<Tag>> hot() {
		Cached<Map<String, List<Tag>>> c = hot;
		if (c != null && fresh(c.at(), LIST_TTL)) return c.value();
		Map<String, List<Tag>> out = new LinkedHashMap<>();
		for (String company : hotCompanies) {
			List<RawRow> rows;
			try {
				rows = raw(company, Window.SIX_MONTHS);
			} catch (FrequencyException e) {
				continue; // one missing list shouldn't hide the rest
			}
			for (RawRow r : rows) out.computeIfAbsent(r.leetcode(), k -> new ArrayList<>()).add(new Tag(company, r.frequency()));
		}
		out.values().forEach(l -> l.sort(Comparator.comparingDouble(Tag::frequency).reversed()));
		hot = new Cached<>(Map.copyOf(out), Instant.now(clock));
		return hot.value();
	}

	public List<String> hotCompanies() {
		return hotCompanies;
	}

	public static String sourceUrl(String folder, Window window) {
		return "https://github.com/" + REPO + "/blob/main/" + enc(folder) + "/" + enc(window.file) + ".csv";
	}

	private List<RawRow> raw(String folder, Window window) {
		String key = folder + "/" + window.file;
		Cached<List<RawRow>> c = lists.get(key);
		if (c != null && fresh(c.at(), LIST_TTL)) return c.value();
		List<RawRow> rows = parse(get(RAW + enc(folder) + "/" + enc(window.file) + ".csv"));
		lists.put(key, new Cached<>(rows, Instant.now(clock)));
		return rows;
	}

	/** Header: Difficulty,Title,Frequency,Acceptance Rate,Link,Topics. */
	static List<RawRow> parse(String csv) {
		List<RawRow> out = new ArrayList<>();
		String[] lines = csv.split("\r?\n");
		for (int i = 1; i < lines.length; i++) {
			List<String> f = fields(lines[i]);
			if (f.size() < 5) continue;
			String link = f.get(4).trim();
			String slug = link.replaceAll("/+$", "").replaceAll(".*/problems/", "");
			if (slug.isBlank() || slug.equals(link)) continue;
			out.add(new RawRow(f.get(1).trim(), slug, cap(f.get(0).trim()), num(f.get(2)), num(f.get(3)),
					f.size() > 5 ? Arrays.stream(f.get(5).split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList() : List.of(),
					link));
		}
		return List.copyOf(out);
	}

	private static List<String> fields(String line) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '"') {
				if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
					cur.append('"');
					i++;
				} else quoted = !quoted;
			} else if (ch == ',' && !quoted) {
				out.add(cur.toString());
				cur.setLength(0);
			} else cur.append(ch);
		}
		out.add(cur.toString());
		return out;
	}

	private static String cap(String s) {
		return s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT);
	}

	private static double num(String s) {
		try {
			return Double.parseDouble(s.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private boolean fresh(Instant at, Duration ttl) {
		return at.plus(ttl).isAfter(Instant.now(clock));
	}

	/** The list couldn't be fetched (offline, renamed folder, GitHub rate limit). */
	public static class FrequencyException extends RuntimeException {
		public FrequencyException(String message) {
			super(message);
		}
	}

	private String get(String url) {
		try {
			HttpResponse<String> res = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
					.header("User-Agent", "CompanyWisePrep").build(), HttpResponse.BodyHandlers.ofString());
			if (res.statusCode() == 404) throw new FrequencyException("No such list on GitHub: " + url);
			if (res.statusCode() / 100 != 2) throw new FrequencyException("GitHub returned HTTP " + res.statusCode());
			return res.body();
		} catch (IOException e) {
			throw new FrequencyException("Could not reach GitHub: " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new FrequencyException("Interrupted while fetching from GitHub");
		}
	}

	private JsonNode getJson(String url) {
		try {
			return json.readTree(get(url));
		} catch (IOException e) {
			throw new FrequencyException("GitHub's folder list was not valid JSON");
		}
	}
}
