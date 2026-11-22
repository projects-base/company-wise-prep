package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Cutting stored text to a column's length without breaking a character in two. */
class TextTest {

	static final String EMOJI = "😀"; // one character, two UTF-16 units

	@Test
	void shortTextIsKept() {
		assertThat(Text.cut("abc", 3)).isEqualTo("abc");
		assertThat(Text.cut(null, 3)).isNull();
	}

	@Test
	void longTextIsCutToTheLimit() {
		assertThat(Text.cut("abcdef", 4)).isEqualTo("abcd");
	}

	@Test
	void aCutNeverSplitsASurrogatePair() {
		assertThat(Text.cut("ab" + EMOJI + "cd", 3)).isEqualTo("ab");
		assertThat(Text.cut("ab" + EMOJI + "cd", 4)).isEqualTo("ab" + EMOJI);
	}

	@Test
	void theCardEllipsisDoesNotSplitAPairEither() {
		// The front has room for 985 characters of code; the cut lands inside the emoji at 983-984.
		String cut = CardService.withCode("Q", "x".repeat(983) + EMOJI + "y".repeat(50));
		assertThat(cut).endsWith("x…\n```");
		assertThat(cut.chars().noneMatch(ch -> Character.isSurrogate((char) ch))).isTrue();
		assertThat(ExplanationService.reference("a".repeat(ExplanationService.REFERENCE_MAX - 1) + EMOJI))
				.hasSize(ExplanationService.REFERENCE_MAX - 1);
	}
}
