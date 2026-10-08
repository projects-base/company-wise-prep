package com.companywiseprep.campaign;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.progress.Progress;
import com.companywiseprep.progress.ProgressService;
import com.companywiseprep.web.NotFoundException;

@Service
public class CampaignService {

	private final CampaignRepository campaigns;
	private final QuestionRepository questions;
	private final ProgressService progress;
	private final Clock clock;

	public CampaignService(CampaignRepository campaigns, QuestionRepository questions, ProgressService progress,
			Clock clock) {
		this.campaigns = campaigns;
		this.questions = questions;
		this.progress = progress;
		this.clock = clock;
	}

	public enum Phase {
		BEFORE_START, STUDY, REVIEW, FINISHED
	}

	public record PlanResponse(Plan plan, Set<String> done) {
	}

	public record Review(String slug, String title, String type, int stage, LocalDate due) {
	}

	/**
	 * Today is the next unfinished items in plan order, not the items dated today. Missed days
	 * never stack into a backlog: the queue simply resumes, and {@code behind} says by how much.
	 */
	public record Today(LocalDate date, Phase phase, boolean weekend, int budget, List<Plan.Item> items,
			List<Review> reviews, int behind, int done, int total, long daysLeft, CampaignConfig campaign) {
	}

	@Transactional(readOnly = true)
	public List<CampaignConfig> list() {
		// Soonest interview first: the UI opens on the first campaign unless one was picked.
		// Campaigns whose interview has passed go last.
		java.time.LocalDate today = java.time.LocalDate.now(clock);
		return campaigns.findAll().stream().map(CampaignConfig::from)
				.sorted(java.util.Comparator.comparing((CampaignConfig c) -> c.interview().isBefore(today))
						.thenComparing(CampaignConfig::interview))
				.toList();
	}

	@Transactional(readOnly = true)
	public PlanResponse plan(String id) {
		return new PlanResponse(build(id), doneSlugs());
	}

	@Transactional(readOnly = true)
	public Today today(String id) {
		Plan plan = build(id);
		CampaignConfig c = plan.campaign();
		Set<String> done = doneSlugs();
		LocalDate today = LocalDate.now(clock);
		boolean weekend = today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY;
		int budget = weekend ? c.weekendMinutes() : c.weekdayMinutes();

		List<Plan.Item> all = plan.days().stream().flatMap(d -> d.items().stream()).toList();
		List<Plan.Item> open = all.stream().filter(i -> !done.contains(i.slug())).toList();

		Phase phase = today.isBefore(c.start()) ? Phase.BEFORE_START
				: !today.isBefore(c.interview()) ? Phase.FINISHED
				: !today.isBefore(c.interview().minusDays(c.reviewDays())) ? Phase.REVIEW
				: Phase.STUDY;

		List<Plan.Item> items = new ArrayList<>();
		if (phase == Phase.STUDY || phase == Phase.BEFORE_START) {
			boolean designLeft = open.stream().anyMatch(i -> Planner.WEEKEND_TYPES.contains(i.type()));
			boolean wantDesign = weekend && designLeft;
			int used = 0;
			for (Plan.Item i : open) {
				if (Planner.WEEKEND_TYPES.contains(i.type()) != wantDesign) continue;
				if (!items.isEmpty() && used + i.minutes() > budget) break;
				items.add(i);
				used += i.minutes();
			}
			// Weekday queue finished but design left (or the reverse): keep going rather than idle.
			if (items.isEmpty() && !open.isEmpty()) items.add(open.get(0));
		}

		int behind = (int) open.stream().filter(i -> i.target() != null && i.target().isBefore(today)).count();
		return new Today(today, phase, weekend, budget, items, reviews(today), behind,
				all.size() - open.size(), all.size(), Math.max(0, ChronoUnit.DAYS.between(today, c.interview())), c);
	}

	private List<Review> reviews(LocalDate today) {
		List<Progress> due = progress.dueReviews(today);
		if (due.isEmpty()) return List.of();
		Map<String, Question> bySlug = questions.findAllById(due.stream().map(Progress::getQuestionSlug).toList())
				.stream().collect(Collectors.toMap(Question::getSlug, Function.identity()));
		return due.stream()
				.filter(p -> bySlug.containsKey(p.getQuestionSlug()))
				.map(p -> {
					Question q = bySlug.get(p.getQuestionSlug());
					return new Review(q.getSlug(), q.getTitle(), q.getType(), p.getReviewStage(), p.getNextReviewOn());
				})
				.toList();
	}

	private Plan build(String id) {
		Campaign campaign = campaigns.findById(id).orElseThrow(() -> new NotFoundException("No campaign " + id));
		return Planner.plan(CampaignConfig.from(campaign), questions.findAllWithSightings());
	}

	private Set<String> doneSlugs() {
		return progress.all().entrySet().stream().filter(e -> e.getValue().done()).map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}
}
