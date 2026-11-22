package com.companywiseprep.notes;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * The spaced-repetition rule, SM-2 style. Pure: the card's state, the grade and the time in; the next state and due
 * time out.
 *
 * A new card (never recalled, or reset by Again) takes fixed first steps: Again 10 minutes, Hard 1 day, Good 4 days,
 * Easy 9 days; the ease does not move. Once graduated, Hard multiplies the interval by 1.2, Good by the ease, Easy by
 * ease × 1.3, and Again resets the card to 10 minutes and counts a lapse; the ease moves (Again −0.2, Hard −0.15,
 * Easy +0.15, never below 1.3), and a multiplier uses the ease from before the grade. A graduated card's interval is in
 * whole days, grows by at least one day, and stops at 180.
 */
final class Scheduler {

	static final double START_EASE = 2.5;
	static final double MIN_EASE = 1.3;
	static final long DAY = Duration.ofDays(1).toMinutes();
	static final long MAX_INTERVAL = 180 * DAY;
	static final long AGAIN_MINUTES = 10;

	private Scheduler() {
	}

	/** @param reps successful recalls since the card was new or last reset */
	record State(int reps, int lapses, double ease, long intervalMinutes) {
		static final State NEW = new State(0, 0, START_EASE, 0);
	}

	record Next(State state, Instant due) {
	}

	static Next next(State s, Grade g, Instant now) {
		double step = switch (g) {
			case AGAIN -> -0.2;
			case HARD -> -0.15;
			case GOOD -> 0;
			case EASY -> 0.15;
		};
		double ease = s.reps() == 0 ? s.ease() : Math.max(MIN_EASE, round2(s.ease() + step));
		State next;
		if (g == Grade.AGAIN) {
			next = new State(0, s.reps() > 0 ? s.lapses() + 1 : s.lapses(), ease, AGAIN_MINUTES);
		} else {
			next = new State(s.reps() + 1, s.lapses(), ease, interval(s, g));
		}
		return new Next(next, now.plus(Duration.ofMinutes(next.intervalMinutes())));
	}

	/** The interval in minutes each grade would give: the labels on the four buttons. */
	static Map<Grade, Long> previews(State s) {
		Map<Grade, Long> out = new EnumMap<>(Grade.class);
		for (Grade g : Grade.values()) out.put(g, next(s, g, Instant.EPOCH).state().intervalMinutes());
		return out;
	}

	private static long interval(State s, Grade g) {
		if (s.reps() == 0) {
			return switch (g) {
				case HARD -> DAY;
				case GOOD -> 4 * DAY;
				default -> 9 * DAY;
			};
		}
		double factor = switch (g) {
			case HARD -> 1.2;
			case GOOD -> s.ease();
			default -> s.ease() * 1.3;
		};
		long previous = Math.max(1, Math.round((double) s.intervalMinutes() / DAY));
		long days = Math.max(previous + 1, Math.round(previous * factor));
		return Math.min(MAX_INTERVAL, days * DAY);
	}

	/** Ease to two decimals, so repeated ±0.15 steps do not drift. */
	private static double round2(double v) {
		return Math.round(v * 100) / 100.0;
	}
}
