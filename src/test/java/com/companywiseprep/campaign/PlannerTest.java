package com.companywiseprep.campaign;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.Sighting;

class PlannerTest {

	private static final LocalDate MONDAY = LocalDate.of(2026, 10, 5);

	private static CampaignConfig campaign(Map<String, Double> companies, LocalDate start, LocalDate interview) {
		return new CampaignConfig("c", "C", "SDE", companies, start, interview, 25, 40, 3, null);
	}

	private static Question q(String slug, String type, String difficulty, String... companySeenOn) {
		Question q = new Question();
		q.setSlug(slug);
		q.setTitle(slug);
		q.setType(type);
		q.setDifficulty(difficulty);
		for (int i = 0; i < companySeenOn.length; i += 2) {
			Sighting s = new Sighting();
			s.setCompany(companySeenOn[i]);
			s.setSeenOn(companySeenOn[i + 1]);
			s.setConfidence("claimed");
			q.addSighting(s);
		}
		return q;
	}

	private static Map<String, Double> targets(String... companies) {
		Map<String, Double> m = new LinkedHashMap<>();
		for (String c : companies) m.put(c, 1.0);
		return m;
	}

	@Test
	void questionsSharedByTargetCompaniesComeFirst() {
		List<Question> bank = List.of(
				q("only-ms", "DSA", "medium", "microsoft", "2026-09"),
				q("shared", "DSA", "medium", "microsoft", "2026-09", "google", "2026-09"));
		Plan plan = Planner.plan(campaign(targets("microsoft", "google"), MONDAY, MONDAY.plusDays(30)), bank);

		assertThat(plan.days().get(0).items()).extracting(Plan.Item::slug).containsExactly("shared");
	}

	@Test
	void questionsNotAskedAtATargetAreLeftOut() {
		List<Question> bank = List.of(q("google-only", "DSA", "medium", "google", "2026-09"));
		Plan plan = Planner.plan(campaign(targets("microsoft"), MONDAY, MONDAY.plusDays(10)), bank);

		assertThat(plan.days()).allSatisfy(d -> assertThat(d.items()).isEmpty());
		assertThat(plan.beyond()).isEmpty();
	}

	@Test
	void recentReportsOutweighOldOnes() {
		LocalDate asOf = LocalDate.of(2026, 10, 1);
		assertThat(Planner.recency("2026-03", asOf)).isEqualTo(1.0);
		assertThat(Planner.recency("2025-01", asOf)).isEqualTo(0.6);
		assertThat(Planner.recency("2022-05", asOf)).isEqualTo(0.3);
		assertThat(Planner.recency("2026", asOf)).isEqualTo(1.0);
		assertThat(Planner.recency(null, asOf)).isEqualTo(0.3);
	}

	@Test
	void designGoesOnWeekendsAndDsaOnWeekdays() {
		List<Question> bank = new ArrayList<>();
		for (int i = 0; i < 10; i++) bank.add(q("dsa-" + i, "DSA", "medium", "microsoft", "2026-09"));
		bank.add(q("lld-1", "LLD", "medium", "microsoft", "2026-09"));
		bank.add(q("hld-1", "HLD", "medium", "microsoft", "2026-09"));
		Plan plan = Planner.plan(campaign(targets("microsoft"), MONDAY, MONDAY.plusDays(20)), bank);

		for (Plan.Day d : plan.days()) {
			boolean weekend = d.date().getDayOfWeek() == DayOfWeek.SATURDAY
					|| d.date().getDayOfWeek() == DayOfWeek.SUNDAY;
			d.items().forEach(i -> assertThat(Planner.WEEKEND_TYPES.contains(i.type())).isEqualTo(weekend));
		}
	}

	@Test
	void everyFourthWeekdaySlotIsAConcept() {
		List<Question> bank = new ArrayList<>();
		for (int i = 0; i < 6; i++) bank.add(q("dsa-" + i, "DSA", "medium", "microsoft", "2026-09"));
		bank.add(q("tcp-vs-udp", "DOMAIN", "medium", "microsoft", "2026-09"));
		Plan plan = Planner.plan(campaign(targets("microsoft"), MONDAY, MONDAY.plusDays(14)), bank);

		List<String> order = plan.days().stream().flatMap(d -> d.items().stream()).map(Plan.Item::slug).toList();
		assertThat(order.indexOf("tcp-vs-udp")).isEqualTo(3);
	}

	@Test
	void anItemLongerThanTheBudgetStillGetsItsOwnDay() {
		List<Question> bank = List.of(q("hard", "DSA", "hard", "microsoft", "2026-09"));
		Plan plan = Planner.plan(campaign(targets("microsoft"), MONDAY, MONDAY.plusDays(10)), bank);

		assertThat(plan.days().get(0).items()).extracting(Plan.Item::minutes).containsExactly(40);
	}

	@Test
	void theLastDaysAreReviewOnly() {
		List<Question> bank = new ArrayList<>();
		for (int i = 0; i < 50; i++) bank.add(q("dsa-" + i, "DSA", "medium", "microsoft", "2026-09"));
		LocalDate interview = MONDAY.plusDays(10);
		Plan plan = Planner.plan(campaign(targets("microsoft"), MONDAY, interview), bank);

		List<Plan.Day> last = plan.days().subList(plan.days().size() - 3, plan.days().size());
		assertThat(last).allSatisfy(d -> {
			assertThat(d.kind()).isEqualTo(Plan.Kind.REVIEW);
			assertThat(d.items()).isEmpty();
		});
		assertThat(plan.days()).last().extracting(Plan.Day::date).isEqualTo(interview.minusDays(1));
		assertThat(plan.beyond()).isNotEmpty();
	}
}
