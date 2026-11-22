package com.companywiseprep.notes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.companywiseprep.notes.NoteService.NoteView;
import com.companywiseprep.notes.NotebookService.LessonNotes;

/** Renders notes as a Markdown revision sheet. Pure: same notes in, same text out; nothing from the lesson text itself. */
final class CheatSheet {

	static final List<String> SUMMARY_LABELS = List.of("Idea", "How it works inside", "The trap", "Interview line");
	private static final Pattern BACKTICKS = Pattern.compile("`{3,}");

	private CheatSheet() {
	}

	static String render(String title, String date, List<LessonNotes> lessons, List<NoteView> general) {
		StringBuilder md = new StringBuilder("# Cheat sheet · ").append(title).append("\n\n")
				.append("_From your own notes, highlights, summaries, explanations and mistakes · ").append(date).append("_\n");
		if (lessons.isEmpty() && general.isEmpty()) {
			return md.append("\nNo notes yet for this scope. Highlight a sentence or write a lesson summary first.\n").toString();
		}
		for (LessonNotes l : lessons) lesson(md, l);
		if (!general.isEmpty()) {
			md.append("\n## General notes\n");
			general.forEach(n -> snippet(md, n));
		}
		return md.toString();
	}

	private static void lesson(StringBuilder md, LessonNotes l) {
		md.append("\n## ").append(l.code()).append(" · ").append(l.title()).append('\n');
		if (l.summary() != null) {
			md.append('\n');
			List<String> values = l.summary().values();
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == null) continue;
				md.append("- **").append(SUMMARY_LABELS.get(i)).append(":** ").append(oneLine(values.get(i))).append('\n');
			}
		}
		if (!l.highlights().isEmpty()) {
			md.append("\n### Highlights\n");
			for (NoteView h : l.highlights()) {
				md.append('\n').append(quote(h.quote())).append('\n');
				if (h.section() != null) md.append(">\n> — *").append(h.section()).append("*\n");
				if (h.note() != null) md.append('\n').append(h.note()).append('\n');
			}
		}
		if (!l.explanations().isEmpty()) {
			md.append("\n### My explanation\n");
			l.explanations().forEach(e -> md.append('\n').append(e.improved().strip()).append('\n'));
		}
		if (!l.snippets().isEmpty()) {
			md.append("\n### From the Playground\n");
			l.snippets().forEach(n -> snippet(md, n));
		}
		if (!l.mistakes().isEmpty()) {
			md.append("\n### Mistakes to avoid\n\n");
			// Already open first (MistakeService orders them that way).
			l.mistakes().forEach(m -> md.append("- ").append(oneLine(m.question())).append(" — not \"")
					.append(oneLine(m.chosen())).append("\"; it is \"").append(oneLine(m.correct())).append('"')
					.append(m.why() == null ? "" : ". " + oneLine(m.why()))
					.append(m.fixed() ? " *(fixed)*" : "").append('\n'));
		}
	}

	private static void snippet(StringBuilder md, NoteView n) {
		if (n.note() != null) md.append('\n').append(n.note()).append('\n');
		md.append('\n').append(fenced("java", n.code()));
		if (n.output() != null) md.append("\nOutput:\n\n").append(fenced("", n.output()));
	}

	private static String quote(String text) {
		return "> " + text.strip().replace("\n", "\n> ");
	}

	private static String oneLine(String s) {
		return s == null ? "" : s.strip().replaceAll("\\s*\\n\\s*", " ");
	}

	/** A fence longer than any backtick run inside, so code containing ``` cannot end it early. */
	private static String fenced(String lang, String body) {
		int longest = 2;
		Matcher m = BACKTICKS.matcher(body);
		while (m.find()) longest = Math.max(longest, m.group().length());
		String fence = "`".repeat(longest + 1);
		return fence + lang + "\n" + body.stripTrailing() + "\n" + fence + "\n";
	}
}
