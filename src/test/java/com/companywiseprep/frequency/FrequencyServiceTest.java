package com.companywiseprep.frequency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.companywiseprep.frequency.FrequencyService.Window;

class FrequencyServiceTest {

	@Test
	void parsesTheRepoCsvIncludingQuotedTopics() {
		var rows = FrequencyService.parse("""
				Difficulty,Title,Frequency,Acceptance Rate,Link,Topics
				EASY,Two Sum,100.0,0.0058,https://leetcode.com/problems/two-sum,"Array, Hash Table"
				HARD,"Median of Two Sorted Arrays, again",61.5,0.44,https://leetcode.com/problems/median-of-two-sorted-arrays/,Binary Search
				broken line
				""");
		assertThat(rows).hasSize(2);
		assertThat(rows.get(0).leetcode()).isEqualTo("two-sum");
		assertThat(rows.get(0).difficulty()).isEqualTo("Easy");
		assertThat(rows.get(0).topics()).containsExactly("Array", "Hash Table");
		assertThat(rows.get(1).title()).isEqualTo("Median of Two Sorted Arrays, again");
		assertThat(rows.get(1).leetcode()).isEqualTo("median-of-two-sorted-arrays");
		assertThat(rows.get(1).frequency()).isEqualTo(61.5);
	}

	@Test
	void windowsAndSourceLinks() {
		assertThat(Window.parse(null)).isEqualTo(Window.THREE_MONTHS);
		assertThat(Window.parse("thirty-days")).isEqualTo(Window.THIRTY_DAYS);
		assertThat(FrequencyService.sourceUrl("Walmart Labs", Window.SIX_MONTHS)).isEqualTo(
				"https://github.com/liquidslr/leetcode-company-wise-problems/blob/main/Walmart%20Labs/3.%20Six%20Months.csv");
	}
}
