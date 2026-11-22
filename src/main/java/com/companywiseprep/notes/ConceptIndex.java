package com.companywiseprep.notes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * [[Concept]] links parsed from note text, built when asked rather than stored: the data is small and parsing keeps the
 * links always in step with the notes. A concept is keyed by its name lowercased with runs of spaces collapsed; the
 * first spelling seen is the one shown. Pure: items in, counts and relations out.
 */
final class ConceptIndex {

	static final int NAME_MAX = 100;
	private static final Pattern LINK = Pattern.compile("\\[\\[([^\\[\\]\\n]{1," + NAME_MAX + "})]]");
	/** A Markdown table row: | first | ... | last |. */
	private static final Pattern ROW = Pattern.compile("^\\s*\\|(.*)\\|\\s*$");
	private static final Pattern CODE = Pattern.compile("[A-Z]+(?:-[A-Z]+)*-\\d+");

	/** Something Akhil wrote: a highlight, a snippet, a summary or an explanation, with the texts that may hold links. */
	record Item(String kind, long id, String moduleId, List<String> texts) {
	}

	record Concept(String key, String name, int count) {
	}

	/** A Reference glossary row: the term and the display codes of the lessons that teach it. */
	record Term(String term, List<String> codes) {

		/** The full term, plus the short name before a bracket: "JVM (Java Virtual Machine)" is linked by [[JVM]] too. */
		List<String> keys() {
			String full = key(term);
			int bracket = term.indexOf(" (");
			String shortName = bracket > 0 ? key(term.substring(0, bracket)) : full;
			return shortName.equals(full) ? List.of(full) : List.of(full, shortName);
		}
	}

	private final List<Item> items;
	private final Map<String, String> names;
	/** The concept keys each item links, in item order. */
	private final List<Set<String>> keysByItem;

	private ConceptIndex(List<Item> items, Map<String, String> names, List<Set<String>> keysByItem) {
		this.items = items;
		this.names = names;
		this.keysByItem = keysByItem;
	}

	static ConceptIndex of(List<Item> items) {
		Map<String, String> names = new LinkedHashMap<>();
		List<Set<String>> keysByItem = new ArrayList<>();
		for (Item item : items) {
			Set<String> keys = new LinkedHashSet<>();
			for (String name : links(item.texts().toArray(String[]::new))) {
				String k = key(name);
				names.putIfAbsent(k, name);
				keys.add(k);
			}
			keysByItem.add(keys);
		}
		return new ConceptIndex(List.copyOf(items), names, keysByItem);
	}

	/** Every [[name]] in the texts, in order, with spaces trimmed and collapsed; blank names are not links. */
	static List<String> links(String... texts) {
		List<String> out = new ArrayList<>();
		for (String t : texts) {
			if (t == null) continue;
			Matcher m = LINK.matcher(t);
			while (m.find()) {
				String name = m.group(1).strip().replaceAll("\\s+", " ");
				if (!name.isEmpty()) out.add(name);
			}
		}
		return out;
	}

	static String key(String name) {
		return name.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	/** Every linked concept with the number of items linking it, strongest first, then by name. */
	List<Concept> concepts() {
		Map<String, Integer> counts = new LinkedHashMap<>();
		keysByItem.forEach(keys -> keys.forEach(k -> counts.merge(k, 1, Integer::sum)));
		return sorted(counts);
	}

	/** Concepts linked by the same items as this one, most shared items first. */
	List<Concept> related(String name) {
		String k = key(name);
		Map<String, Integer> counts = new LinkedHashMap<>();
		keysByItem.stream().filter(keys -> keys.contains(k))
				.forEach(keys -> keys.stream().filter(x -> !x.equals(k)).forEach(x -> counts.merge(x, 1, Integer::sum)));
		return sorted(counts);
	}

	List<Item> itemsFor(String name) {
		String k = key(name);
		List<Item> out = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			if (keysByItem.get(i).contains(k)) out.add(items.get(i));
		}
		return out;
	}

	/** The keys of every linked concept. */
	Set<String> linked() {
		return names.keySet();
	}

	/** The display name for a key, when the concept is linked anywhere. */
	String name(String name) {
		return names.get(key(name));
	}

	private List<Concept> sorted(Map<String, Integer> counts) {
		return counts.entrySet().stream()
				.map(e -> new Concept(e.getKey(), names.get(e.getKey()), e.getValue()))
				.sorted(Comparator.comparingInt(Concept::count).reversed().thenComparing(Concept::key))
				.toList();
	}

	/** Whether the text names the term as a whole word (case and spacing ignored), so HashMap is not in ConcurrentHashMap. */
	static boolean mentions(String text, String term) {
		return text != null && mentionPattern(term).matcher(text).find();
	}

	static Pattern mentionPattern(String term) {
		String body = Stream.of(term.strip().split("\\s+")).map(Pattern::quote).reduce((a, b) -> a + "\\s+" + b).orElse("");
		return Pattern.compile("(?<![\\p{L}\\p{N}_])" + body + "(?![\\p{L}\\p{N}_])",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	/**
	 * The terms of the Reference glossary: the first cell of every table row, with the lesson codes in its last cell
	 * ("—" or no code means no lesson yet). Header and separator rows are skipped.
	 */
	static List<Term> glossary(String md) {
		List<Term> out = new ArrayList<>();
		if (md == null) return out;
		for (String line : md.split("\n")) {
			Matcher row = ROW.matcher(line);
			if (!row.matches()) continue;
			String[] cells = row.group(1).split("\\|", -1);
			String term = cells[0].strip();
			if (cells.length < 2 || term.isEmpty() || term.matches("-+|:?-+:?") || term.equalsIgnoreCase("Term")) continue;
			List<String> codes = new ArrayList<>();
			Matcher c = CODE.matcher(cells[cells.length - 1]);
			while (c.find()) codes.add(c.group());
			out.add(new Term(term, codes));
		}
		return out;
	}
}
