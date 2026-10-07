package com.companywiseprep.campaign;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.Sighting;

/**
 * Turns a campaign and the bank into a day-by-day plan. Pure and deterministic: the same
 * inputs always give the same plan, and nothing here depends on today's date — recency is
 * measured from the campaign's start. Rules: docs/COMPANY-PREP.md §4.
 */
public final class Planner {

	/** Design work needs a longer, calmer block, so it goes on weekends. */
	static final Set<String> WEEKEND_TYPES = Set.of("LLD", "HLD");

	/** Weekday slots run DSA, DSA, DSA, then one concept/behavioural item. */
	private static final int CONCEPT_EVERY = 4;

	private Planner() {
	}

	public static Plan plan(CampaignConfig c, List<Question> bank) {
		List<Plan.Item> ranked = bank.stream()
				.filter(q -> q.getSightings().stream().anyMatch(s -> c.companies().containsKey(s.getCompany())))
				.map(q -> item(q, c))
				.sorted(Comparator.comparingDouble(Plan.Item::priority).reversed()
						.thenComparingInt(i -> difficultyOrder(i.difficulty()))
						.thenComparing(Plan.Item::slug))
				.toList();

		Deque<Plan.Item> weekday = weekdayQueue(ranked);
		Deque<Plan.Item> weekend = new ArrayDeque<>(
				ranked.stream().filter(i -> WEEKEND_TYPES.contains(i.type())).toList());

		LocalDate reviewFrom = c.interview().minusDays(c.reviewDays());
		List<Plan.Day> days = new ArrayList<>();
		for (LocalDate d = c.start(); d.isBefore(c.interview()); d = d.plusDays(1)) {
			boolean isWeekend = d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
			int budget = isWeekend ? c.weekendMinutes() : c.weekdayMinutes();
			if (!d.isBefore(reviewFrom)) {
				days.add(new Plan.Day(d, Plan.Kind.REVIEW, isWeekend, budget, List.of()));
				continue;
			}
			Deque<Plan.Item> queue = isWeekend && !weekend.isEmpty() ? weekend : weekday;
			days.add(new Plan.Day(d, Plan.Kind.STUDY, isWeekend, budget, fill(queue, budget, d)));
		}

		List<Plan.Item> beyond = new ArrayList<>(weekday);
		beyond.addAll(weekend);
		beyond.sort(Comparator.comparingDouble(Plan.Item::priority).reversed());
		return new Plan(c, days, beyond);
	}

	/** At least one item a day, even when it overruns the budget: an item stays queued until done. */
	private static List<Plan.Item> fill(Deque<Plan.Item> queue, int budget, LocalDate day) {
		List<Plan.Item> items = new ArrayList<>();
		int used = 0;
		while (!queue.isEmpty() && (items.isEmpty() || used + queue.peek().minutes() <= budget)) {
			Plan.Item next = queue.poll();
			items.add(next.on(day));
			used += next.minutes();
		}
		return items;
	}

	private static Deque<Plan.Item> weekdayQueue(List<Plan.Item> ranked) {
		Deque<Plan.Item> dsa = new ArrayDeque<>(ranked.stream().filter(i -> i.type().equals("DSA")).toList());
		Deque<Plan.Item> concept = new ArrayDeque<>(ranked.stream()
				.filter(i -> !i.type().equals("DSA") && !WEEKEND_TYPES.contains(i.type())).toList());
		Deque<Plan.Item> out = new ArrayDeque<>();
		int slot = 0;
		while (!dsa.isEmpty() || !concept.isEmpty()) {
			boolean conceptTurn = ++slot % CONCEPT_EVERY == 0;
			Deque<Plan.Item> from = (conceptTurn && !concept.isEmpty()) || dsa.isEmpty() ? concept : dsa;
			out.add(from.poll());
		}
		return out;
	}

	static Plan.Item item(Question q, CampaignConfig c) {
		double priority = 0;
		for (Sighting s : q.getSightings()) {
			Double weight = c.companies().get(s.getCompany());
			if (weight == null) continue;
			priority += weight * recency(s.getSeenOn(), c.start()) * confidence(s.getConfidence());
		}
		List<String> companies = q.getSightings().stream().map(Sighting::getCompany).distinct().sorted().toList();
		// Asked elsewhere too: a small nudge towards questions the industry treats as canonical.
		long elsewhere = companies.stream().filter(co -> !c.companies().containsKey(co)).count();
		priority += 0.1 * elsewhere;
		return new Plan.Item(q.getSlug(), q.getTitle(), q.getType(), q.getDifficulty(), q.getLeetcode(),
				minutes(q.getType(), q.getDifficulty()), Math.round(priority * 100) / 100.0, companies, null);
	}

	static double recency(String seenOn, LocalDate asOf) {
		YearMonth seen = parseMonth(seenOn);
		if (seen == null) return 0.3;
		long months = ChronoUnit.MONTHS.between(seen, YearMonth.from(asOf));
		return months <= 12 ? 1.0 : months <= 24 ? 0.6 : 0.3;
	}

	static double confidence(String confidence) {
		return "verified".equals(confidence) ? 1.0 : 0.7;
	}

	/** Estimated minutes for a first serious attempt. */
	static int minutes(String type, String difficulty) {
		return switch (type) {
			case "DSA" -> switch (difficulty == null ? "medium" : difficulty) {
				case "easy" -> 15;
				case "hard" -> 40;
				default -> 25;
			};
			case "LLD" -> 40;
			case "HLD" -> 45;
			default -> 15;
		};
	}

	/** Medium first — it is what interviews mostly ask — then easy, then hard. */
	private static int difficultyOrder(String d) {
		return Map.of("medium", 0, "easy", 1, "hard", 2).getOrDefault(d, 1);
	}

	private static YearMonth parseMonth(String s) {
		if (s == null || s.isBlank()) return null;
		try {
			return s.length() == 4 ? YearMonth.of(Integer.parseInt(s), 1) : YearMonth.parse(s.substring(0, 7));
		} catch (RuntimeException e) {
			return null;
		}
	}
}
