package com.companywiseprep.lifeline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.companywiseprep.lifeline.LifelineService.Kind;

class LifelineServiceTest {

	private static final String ANSWER = """
			**Short answer:** A bean is created, wired, initialised, used and destroyed by the container.

			## Picture it
			A factory line.

			## The phases
			Instantiate, populate, aware callbacks.

			## Pitfalls
			Self-invocation skips the proxy.
			""";

	@Test
	void withoutAKeyTheNudgeListsTheSectionsAndTheIdeaIsTheShortAnswer() {
		String nudge = LifelineService.offline(Kind.NUDGE, ANSWER);
		assertThat(nudge).contains("- The phases", "- Pitfalls").doesNotContain("Picture it", "container");

		assertThat(LifelineService.offline(Kind.IDEA, ANSWER))
				.isEqualTo("A bean is created, wired, initialised, used and destroyed by the container.");
		assertThat(LifelineService.offline(Kind.NUDGE, null)).startsWith("Start with a one-line definition");
	}
}
