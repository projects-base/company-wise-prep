package com.companywiseprep.review;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The free, private judge: pulls the key terms out of the model answer (bold phrases and inline
 * code, by section) and checks which ones the spoken answer covers. It cannot tell whether an
 * explanation is correct, only whether the important ideas were mentioned; the Claude judge
 * replaces it when a key is set.
 */
final class OfflineReviewer {

	// Whole spans first, length checked after, so a long code span can't shift the backtick pairing.
	private static final Pattern TERM = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`");
	private static final Pattern EXPERIENCE = Pattern.compile(
			"\\b(i used|i have used|i've used|we used|we use|i built|we built|in my (project|team|company|current)|in our (project|team|service|system)|at work|in production|my project|our project)\\b");

	private OfflineReviewer() {
	}

	static AnswerReview review(String modelAnswer, List<String> followUps, String answer) {
		String said = norm(answer);
		int words = answer.isBlank() ? 0 : answer.trim().split("\\s+").length;
		Map<String, List<String>> terms = termsByLayer(modelAnswer == null ? "" : modelAnswer);

		List<String> covered = new ArrayList<>();
		List<String> missed = new ArrayList<>();
		List<AnswerReview.Layer> layers = new ArrayList<>();
		double weighted = 0;
		double weights = 0;
		for (String key : List.of("what", "how", "where", "pitfalls")) {
			Double score;
			String comment;
			if (key.equals("where")) {
				boolean example = EXPERIENCE.matcher(said).find();
				score = example ? 8.0 : 1.0;
				comment = example ? "You tied it to real use." : "No real example. Add one line on where you used it.";
			} else {
				List<String> list = terms.get(key);
				if (list.isEmpty()) {
					layers.add(new AnswerReview.Layer(key, name(key), null, "Nothing to compare offline."));
					continue;
				}
				int hit = 0;
				for (String t : list) {
					if (mentions(said, t)) {
						hit++;
						covered.add(t);
					} else {
						missed.add(t);
					}
				}
				score = Math.round(100.0 * hit / list.size()) / 10.0;
				comment = hit + " of " + list.size() + " key points mentioned.";
			}
			double w = switch (key) {
				case "what", "how" -> 0.3;
				case "where" -> 0.15;
				default -> 0.25;
			};
			weighted += w * score;
			weights += w;
			layers.add(new AnswerReview.Layer(key, name(key), score, comment));
		}
		double score = weights == 0 ? 0 : weighted / weights;
		String verdict;
		if (words < 25) {
			score = Math.min(score, 4);
			verdict = "Too short: in an interview, aim for 60 to 90 seconds covering all four layers.";
		} else if (words > 260) {
			verdict = "Lots of content, but long: lead with the one-line definition and cut to about 90 seconds.";
		} else {
			verdict = score >= 7 ? "Good coverage of the key points." : score >= 5
					? "The basics are there; add the missing points below."
					: "Several key ideas are missing; read the model answer, then try again.";
		}
		score = Math.round(score * 10) / 10.0;
		String improved = shortAnswer(modelAnswer);
		String followUp = followUps.isEmpty() ? null : followUps.get(0);
		return new AnswerReview("offline", score, verdict, layers, covered, missed.stream().limit(8).toList(), List.of(),
				improved, followUp, null, List.of(), null);
	}

	/** Bold and code terms of the model answer, grouped by the layer their section belongs to. */
	static Map<String, List<String>> termsByLayer(String md) {
		Map<String, List<String>> out = new LinkedHashMap<>();
		out.put("what", new ArrayList<>());
		out.put("how", new ArrayList<>());
		out.put("pitfalls", new ArrayList<>());
		String layer = "what";
		boolean fenced = false;
		for (String line : md.split("\n")) {
			if (line.trim().startsWith("```")) {
				fenced = !fenced;
				continue;
			}
			if (fenced) continue;
			if (line.startsWith("## ")) {
				String h = line.substring(3).toLowerCase(Locale.ROOT);
				layer = h.matches(".*(pitfall|follow-up|trade-off|edge case|mistake|gotcha|watch out).*") ? "pitfalls"
						: h.matches(".*(watch it|picture it|deeper|example).*") ? null : "how";
				continue;
			}
			if (layer == null) continue;
			Matcher m = TERM.matcher(line);
			while (m.find()) {
				String raw = (m.group(1) != null ? m.group(1) : m.group(2)).replace("`", "").trim();
				if (raw.length() < 2 || raw.length() > 60) continue;
				// "**Terms.**", "**How the proxy is made.**", "**JDK proxy vs CGLIB?**" are paragraph labels, not points.
				if (m.group(1) != null && raw.matches(".*[.?:]$")) continue;
				// Code expressions (pointcuts, calls with arguments, statements) can't be said out loud.
				if (raw.matches(".*([*{}=;]|\\([^)]).*")) continue;
				String t = raw.replaceAll("[:.,]+$", "").trim();
				if (t.equalsIgnoreCase("short answer") || t.length() < 2 || t.split("\\s+").length > 6) continue;
				// Each term counts once, in the first layer that names it.
				if (out.values().stream().flatMap(List::stream).anyMatch(x -> x.equalsIgnoreCase(t))) continue;
				List<String> list = out.get(layer);
				if (list.size() < 10) list.add(t);
			}
		}
		return out;
	}

	/** A term counts when it appears, or when most of its longer words do (speech rarely repeats it exactly). */
	static boolean mentions(String said, String term) {
		String t = norm(term);
		if (t.isBlank()) return false;
		if (said.contains(t)) return true;
		String[] parts = t.split(" ");
		long sig = 0;
		long hit = 0;
		for (String p : parts) {
			if (p.length() < 4) continue;
			sig++;
			if (said.contains(p)) hit++;
		}
		return sig > 0 && hit * 10 >= sig * 6;
	}

	static String norm(String s) {
		// Split camelCase before lowercasing: `ApplicationContext` is said as "application context".
		return s.replaceAll("([a-z])([A-Z])", "$1 $2")
				.toLowerCase(Locale.ROOT)
				.replaceAll("@", " ")
				.replaceAll("\\(\\)", "")
				.replaceAll("[^a-z0-9#+]+", " ")
				.trim();
	}

	private static String shortAnswer(String md) {
		if (md == null) return null;
		Matcher m = Pattern.compile("\\*\\*Short answer:\\*\\*\\s*([\\s\\S]*?)(?:\\n\\s*\\n|$)").matcher(md);
		return m.find() ? m.group(1).replaceAll("\\s*\\n\\s*", " ").trim() : null;
	}

	static String name(String key) {
		return switch (key) {
			case "what" -> "What it is";
			case "how" -> "How it works";
			case "where" -> "Where you used it";
			default -> "What goes wrong";
		};
	}
}
