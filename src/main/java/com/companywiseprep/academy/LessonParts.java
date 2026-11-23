package com.companywiseprep.academy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A lesson's "## " part titles, split exactly as the lesson page does (frontend Stepper.splitParts). */
final class LessonParts {

	private static final Pattern FENCE = Pattern.compile("^\\s*```.*");
	private static final Pattern PART = Pattern.compile("^##\\s+(.+?)\\s*$");

	private LessonParts() {
	}

	/** Headings inside code fences are not parts; Markdown emphasis (*, _, `) is dropped from titles. */
	static List<String> titles(String lesson) {
		List<String> titles = new ArrayList<>();
		if (lesson == null) return titles;
		boolean fenced = false;
		for (String line : lesson.split("\n", -1)) {
			if (FENCE.matcher(line).matches()) fenced = !fenced;
			Matcher h = fenced ? null : PART.matcher(line);
			if (h != null && h.matches()) titles.add(h.group(1).replaceAll("[*_`]", ""));
		}
		return titles;
	}
}
