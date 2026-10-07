package com.companywiseprep.campaign;

import java.time.LocalDate;
import java.util.List;

/** A campaign's schedule: one entry per day from start to the day before the interview. */
public record Plan(CampaignConfig campaign, List<Day> days, List<Item> beyond) {

	public enum Kind {
		/** New material from the queue. */
		STUDY,
		/** The last days before the interview: reviews and mocks only. */
		REVIEW
	}

	public record Day(LocalDate date, Kind kind, boolean weekend, int budget, List<Item> items) {
	}

	/**
	 * @param companies every company that has asked it — not only the campaign's targets
	 * @param target    the day the plan schedules it for (null for items beyond the plan)
	 */
	public record Item(String slug, String title, String type, String difficulty, String leetcode, int minutes,
			double priority, List<String> companies, LocalDate target) {

		Item on(LocalDate day) {
			return new Item(slug, title, type, difficulty, leetcode, minutes, priority, companies, day);
		}
	}
}
