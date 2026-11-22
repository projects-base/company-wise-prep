package com.companywiseprep.notes;

/** Incoming text, made safe to store: Postgres rejects NUL (U+0000) in text columns, so it is dropped. */
final class Text {

	private Text() {
	}

	static String clean(String s) {
		return s == null || s.indexOf('\u0000') < 0 ? s : s.replace("\u0000", "");
	}

	/** At most {@code max} UTF-16 units, never ending on the first half of a surrogate pair (an emoji cut in two). */
	static String cut(String s, int max) {
		if (s == null || s.length() <= max) return s;
		return s.substring(0, Character.isHighSurrogate(s.charAt(max - 1)) ? max - 1 : max);
	}

	/** Cleaned, stripped, and null when blank. */
	static String trim(String s) {
		String c = clean(s);
		return c == null || c.isBlank() ? null : c.strip();
	}
}
