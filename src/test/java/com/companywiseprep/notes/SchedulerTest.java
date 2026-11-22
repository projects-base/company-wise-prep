package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.companywiseprep.notes.Scheduler.Next;
import com.companywiseprep.notes.Scheduler.State;

/** The spaced-repetition rule: a pure function of the card's state, the grade and the time it was graded. */
class SchedulerTest {

	static final Instant NOW = Instant.parse("2026-11-25T12:00:00Z");
	static final long DAY = 24 * 60;

	private static State learned(long days, double ease) {
		return new State(3, 0, ease, days * DAY);
	}

	// ── A new card: fixed first steps ─────────────────────────────────────────────────────────

	@Test
	void aNewCardStartsAtEaseTwoAndAHalfWithNoInterval() {
		assertThat(State.NEW).isEqualTo(new State(0, 0, 2.5, 0));
	}

	@Test
	void againOnANewCardComesBackInTenMinutes() {
		Next n = Scheduler.next(State.NEW, Grade.AGAIN, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(10);
		assertThat(n.due()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
		assertThat(n.state().reps()).isZero();
		assertThat(n.state().lapses()).as("a card never learned has not lapsed").isZero();
		assertThat(n.state().ease()).isEqualTo(2.5);
	}

	@Test
	void hardOnANewCardIsOneDay() {
		Next n = Scheduler.next(State.NEW, Grade.HARD, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(DAY);
		assertThat(n.due()).isEqualTo(NOW.plus(Duration.ofDays(1)));
		assertThat(n.state().reps()).isEqualTo(1);
		assertThat(n.state().ease()).isEqualTo(2.5);
	}

	@Test
	void goodOnANewCardIsFourDays() {
		Next n = Scheduler.next(State.NEW, Grade.GOOD, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(4 * DAY);
		assertThat(n.state().reps()).isEqualTo(1);
		assertThat(n.state().ease()).isEqualTo(2.5);
	}

	@Test
	void easyOnANewCardIsNineDays() {
		Next n = Scheduler.next(State.NEW, Grade.EASY, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(9 * DAY);
		assertThat(n.state().ease()).isEqualTo(2.5);
	}

	@Test
	void aCardAgainedBackToNewTakesTheNewStepsNext() {
		State relearning = Scheduler.next(learned(10, 2.5), Grade.AGAIN, NOW).state();
		assertThat(Scheduler.next(relearning, Grade.GOOD, NOW).state().intervalMinutes()).isEqualTo(4 * DAY);
	}

	// ── Later reviews: SM-2 style ─────────────────────────────────────────────────────────────

	@Test
	void againOnALearnedCardResetsItCountsALapseAndLowersEase() {
		Next n = Scheduler.next(learned(20, 2.5), Grade.AGAIN, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(10);
		assertThat(n.state().reps()).isZero();
		assertThat(n.state().lapses()).isEqualTo(1);
		assertThat(n.state().ease()).isEqualTo(2.3);
	}

	@Test
	void hardMultipliesTheIntervalByOnePointTwoAndLowersEase() {
		Next n = Scheduler.next(learned(10, 2.5), Grade.HARD, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(12 * DAY);
		assertThat(n.state().ease()).isEqualTo(2.35);
		assertThat(n.state().reps()).isEqualTo(4);
	}

	@Test
	void goodMultipliesTheIntervalByEaseAndKeepsEase() {
		Next n = Scheduler.next(learned(4, 2.5), Grade.GOOD, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(10 * DAY);
		assertThat(n.due()).isEqualTo(NOW.plus(Duration.ofDays(10)));
		assertThat(n.state().ease()).isEqualTo(2.5);
	}

	@Test
	void easyMultipliesByEaseTimesOnePointThreeWithTheOldEaseThenRaisesIt() {
		Next n = Scheduler.next(learned(4, 2.5), Grade.EASY, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(13 * DAY); // 4 × 2.5 × 1.3 = 13
		assertThat(n.state().ease()).isEqualTo(2.65);
	}

	@Test
	void intervalsRoundToWholeDays() {
		assertThat(Scheduler.next(learned(3, 2.5), Grade.HARD, NOW).state().intervalMinutes()).isEqualTo(4 * DAY); // 3.6
	}

	@Test
	void aGraduatedCardAlwaysGrowsByAtLeastADay() {
		assertThat(Scheduler.next(learned(1, 1.3), Grade.GOOD, NOW).state().intervalMinutes()).isEqualTo(2 * DAY);
		assertThat(Scheduler.next(learned(1, 2.5), Grade.HARD, NOW).state().intervalMinutes()).isEqualTo(2 * DAY);
		assertThat(Scheduler.next(learned(179, 1.3), Grade.HARD, NOW).state().intervalMinutes()).isEqualTo(180 * DAY);
	}

	@Test
	void repeatedAgainOnANewCardLeavesEaseAlone() {
		State s = State.NEW;
		for (int i = 0; i < 5; i++) s = Scheduler.next(s, Grade.AGAIN, NOW).state();
		assertThat(s.ease()).isEqualTo(2.5);
		assertThat(s.lapses()).isZero();
	}

	@Test
	void easeNeverFallsBelowTheFloor() {
		assertThat(Scheduler.next(learned(5, 1.4), Grade.AGAIN, NOW).state().ease()).isEqualTo(1.3);
		assertThat(Scheduler.next(learned(5, 1.3), Grade.HARD, NOW).state().ease()).isEqualTo(1.3);
	}

	@Test
	void theIntervalIsCappedAtOneHundredEightyDays() {
		Next n = Scheduler.next(learned(100, 2.5), Grade.EASY, NOW);
		assertThat(n.state().intervalMinutes()).isEqualTo(180 * DAY);
		assertThat(n.due()).isEqualTo(NOW.plus(Duration.ofDays(180)));
	}

	@Test
	void repeatedEasyRaisesEaseOnceTheCardHasGraduated() {
		State s = State.NEW;
		for (int i = 0; i < 4; i++) s = Scheduler.next(s, Grade.EASY, NOW).state();
		assertThat(s.ease()).isEqualTo(2.95);
		assertThat(s.reps()).isEqualTo(4);
	}

	@Test
	void previewsGiveTheNextIntervalForEveryGrade() {
		Map<Grade, Long> p = Scheduler.previews(State.NEW);
		assertThat(p).containsExactly(Map.entry(Grade.AGAIN, 10L), Map.entry(Grade.HARD, DAY), Map.entry(Grade.GOOD, 4 * DAY),
				Map.entry(Grade.EASY, 9 * DAY));
	}
}
