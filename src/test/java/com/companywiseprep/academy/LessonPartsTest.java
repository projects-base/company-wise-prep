package com.companywiseprep.academy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Counts a lesson's "## " parts the way the lesson page splits them (frontend Stepper.splitParts). */
class LessonPartsTest {

	@Test
	void partsAreTheLevelTwoHeadingsOutsideCodeFences() {
		String lesson = """
				Intro text.
				## The **problem**
				Body.
				```java
				## not a part (inside a fence)
				```
				### A sub-heading, not a part
				##   Where `we` are\s\s
				""";
		assertThat(LessonParts.titles(lesson)).containsExactly("The problem", "Where we are");
	}

	@Test
	void aLessonWithoutPartsHasNone() {
		assertThat(LessonParts.titles("")).isEmpty();
		assertThat(LessonParts.titles(null)).isEmpty();
	}
}
