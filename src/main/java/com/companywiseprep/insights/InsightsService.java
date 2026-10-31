package com.companywiseprep.insights;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.academy.AcademyPath;
import com.companywiseprep.academy.AcademyPathRepository;
import com.companywiseprep.academy.LessonProgress;
import com.companywiseprep.academy.LessonProgressRepository;
import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.campaign.CampaignConfig;
import com.companywiseprep.campaign.CampaignRepository;
import com.companywiseprep.progress.Progress;
import com.companywiseprep.progress.ProgressRepository;
import com.companywiseprep.review.ReviewAttempt;
import com.companywiseprep.review.ReviewAttemptRepository;

/**
 * Evidence of progress, computed from what is already stored: lessons done, questions done, judged
 * answers and stories. Read-only; nothing here writes.
 */
@Service
public class InsightsService {

	static final String MAIN_PATH = "zero-to-pro";
	static final int STORY_TARGET = 5;
	static final double WIN_SCORE = 8;
	static final double WEAK_SCORE = 6;

	/** An interview area: the question types and Academy tracks that build it. */
	enum Area {
		DSA("dsa", "DSA", Set.of("DSA"), Set.of("C")),
		JAVA("java", "Java", Set.of("JAVA"), Set.of("J", "A", "B", "L")),
		SPRING("spring", "Spring & web", Set.of("SPRING"), Set.of("D", "W", "S")),
		SQL("sql", "Databases & SQL", Set.of("SQL"), Set.of("Q")),
		LLD("lld", "LLD", Set.of("LLD"), Set.of("E")),
		HLD("hld", "System design", Set.of("HLD", "DOMAIN"), Set.of("F", "I", "G")),
		BEHAVIORAL("behavioral", "Behavioural", Set.of("BEHAVIORAL"), Set.of("H"));

		final String key;
		final String label;
		final Set<String> types;
		final Set<String> tracks;

		Area(String key, String label, Set<String> types, Set<String> tracks) {
			this.key = key;
			this.label = label;
			this.types = types;
			this.tracks = tracks;
		}
	}

	public record Count(int done, int total) {
		double ratio() {
			return total == 0 ? 0 : (double) done / total;
		}
	}

	/** Done against what the plan scheduled for the week. */
	public record Planned(int done, int planned) {
	}

	public record Judged(Double average, int count) {
	}

	/** What to do next for an area: kind is lesson, practice, redo or story. */
	public record Next(String kind, String label, String link) {
	}

	/** @param stories behavioural only; null elsewhere */
	public record AreaView(String key, String name, int score, String status, Count lessons, Count practice,
			Judged judged, Integer stories, Double trend, Next next) {
	}

	public record Readiness(List<AreaView> areas, int overall) {
	}

	public record Scored(String slug, String title, double score) {
	}

	public record Improved(String slug, String title, double from, double to) {
	}

	public record Week(LocalDate from, LocalDate to, Planned lessons, Planned practice, Judged answers,
			List<Improved> improved, List<Scored> weakest, int activeDays) {
	}

	public record Win(LocalDate date, String kind, String title, String link) {
	}

	private final QuestionRepository questions;
	private final AcademyModuleRepository modules;
	private final AcademyPathRepository paths;
	private final LessonProgressRepository lessonProgress;
	private final ProgressRepository progress;
	private final ReviewAttemptRepository attempts;
	private final CampaignRepository campaigns;
	private final StoryRepository stories;
	private final Clock clock;

	public InsightsService(QuestionRepository questions, AcademyModuleRepository modules, AcademyPathRepository paths,
			LessonProgressRepository lessonProgress, ProgressRepository progress, ReviewAttemptRepository attempts,
			CampaignRepository campaigns, StoryRepository stories, Clock clock) {
		this.questions = questions;
		this.modules = modules;
		this.paths = paths;
		this.lessonProgress = lessonProgress;
		this.progress = progress;
		this.attempts = attempts;
		this.campaigns = campaigns;
		this.stories = stories;
		this.clock = clock;
	}

	// ── Readiness ─────────────────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public Readiness readiness(String campaignId) {
		Snapshot s = snapshot(campaignId);
		List<AreaView> areas = Arrays.stream(Area.values()).map(a -> area(a, s)).toList();
		int overall = (int) Math.round(areas.stream().mapToInt(AreaView::score).average().orElse(0));
		return new Readiness(areas, overall);
	}

	private AreaView area(Area a, Snapshot s) {
		List<AcademyModule> areaLessons = s.lessonOrder.stream().filter(m -> a.tracks.contains(m.getTrackId())).toList();
		Count lessons = new Count((int) areaLessons.stream().filter(m -> s.lessonsDone.containsKey(m.getId())).count(),
				areaLessons.size());

		List<String> planned = s.planQuestions.stream().filter(slug -> inArea(a, s.bySlug.get(slug))).toList();
		if (planned.isEmpty()) {
			planned = s.bySlug.values().stream().filter(q -> inArea(a, q)).map(Question::getSlug).sorted().toList();
		}
		Count practice = new Count((int) planned.stream().filter(s.questionsDone::containsKey).count(), planned.size());

		Map<String, Double> latest = s.latestScore.entrySet().stream().filter(e -> inArea(a, s.bySlug.get(e.getKey())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Judged judged = new Judged(latest.isEmpty() ? null : round1(avg(latest.values().stream())), latest.size());
		Integer storyCount = a == Area.BEHAVIORAL ? s.storyCount : null;

		List<Double> parts = new ArrayList<>();
		if (lessons.total() > 0) parts.add(lessons.ratio());
		if (practice.total() > 0) parts.add(practice.ratio());
		if (judged.count() > 0) parts.add(judged.average() / 10);
		if (storyCount != null) parts.add(Math.min(1.0, (double) storyCount / STORY_TARGET));
		int score = (int) Math.round(100 * parts.stream().mapToDouble(Double::doubleValue).average().orElse(0));
		String status = score >= 75 ? "ready" : score >= 45 ? "getting-there" : "gap";

		return new AreaView(a.key, a.label, score, status, lessons, practice, judged, storyCount, trend(a, s),
				next(a, s, areaLessons, planned, lessons, practice, latest, storyCount));
	}

	/**
	 * A weak judged answer comes first (redo it); otherwise the part with the lowest ratio: lessons,
	 * then planned practice, then stories. Null when everything in the area is done.
	 */
	private Next next(Area a, Snapshot s, List<AcademyModule> areaLessons, List<String> planned, Count lessons,
			Count practice, Map<String, Double> latest, Integer storyCount) {
		Optional<Map.Entry<String, Double>> weakest = latest.entrySet().stream().filter(e -> e.getValue() < WEAK_SCORE)
				.min(Map.Entry.comparingByValue());
		if (weakest.isPresent()) {
			String slug = weakest.get().getKey();
			return new Next("redo", "Answer again: " + title(s, slug) + " (last " + round1(weakest.get().getValue()) + "/10)",
					"/q/" + slug);
		}
		Optional<AcademyModule> lesson = areaLessons.stream().filter(m -> !s.lessonsDone.containsKey(m.getId())).findFirst();
		Optional<String> question = planned.stream().filter(slug -> !s.questionsDone.containsKey(slug)).findFirst();
		double storyRatio = storyCount == null ? 2 : Math.min(1.0, (double) storyCount / STORY_TARGET);
		double lessonRatio = lesson.isPresent() ? lessons.ratio() : 2;
		double practiceRatio = question.isPresent() ? practice.ratio() : 2;
		double min = Math.min(lessonRatio, Math.min(practiceRatio, storyRatio));
		if (min >= 1) return null;
		if (lessonRatio == min) {
			AcademyModule m = lesson.get();
			return new Next("lesson", "Read " + m.getCode() + ": " + m.getTitle(), "/academy/" + m.getId());
		}
		if (practiceRatio == min) {
			String slug = question.get();
			return new Next("practice", "Practise: " + title(s, slug), "/q/" + slug);
		}
		return new Next("story", "Write a STAR story for a behavioural question", "/progress/stories");
	}

	/** Average judged score over the last 14 days minus the 14 days before; null without both. */
	private Double trend(Area a, Snapshot s) {
		LocalDate today = LocalDate.now(clock);
		List<ReviewAttempt> mine = s.attempts.stream().filter(r -> inArea(a, s.bySlug.get(r.getQuestionSlug()))).toList();
		List<Double> recent = new ArrayList<>();
		List<Double> before = new ArrayList<>();
		for (ReviewAttempt r : mine) {
			long age = today.toEpochDay() - day(r).toEpochDay();
			if (age < 14) recent.add(r.getScore());
			else if (age < 28) before.add(r.getScore());
		}
		return recent.isEmpty() || before.isEmpty() ? null : round1(avg(recent.stream()) - avg(before.stream()));
	}

	// ── The week ──────────────────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public Week week(String campaignId, LocalDate date) {
		Snapshot s = snapshot(campaignId);
		LocalDate from = (date == null ? LocalDate.now(clock) : date).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate to = from.plusDays(6);
		List<CampaignConfig.Session> sessions = s.sessions.stream().filter(x -> !x.date().isBefore(from) && !x.date().isAfter(to))
				.toList();

		int lessonsPlanned = (int) sessions.stream().filter(x -> x.title().startsWith("Scratch · Step")).count();
		int lessonsDone = (int) s.lessonsDone.values().stream().filter(d -> within(d, from, to)).count();
		int practicePlanned = (int) sessions.stream().filter(CampaignConfig.Session::core).flatMap(x -> x.questions().stream())
				.distinct().count();
		int practiceDone = (int) s.questionsDone.values().stream().filter(d -> within(d, from, to)).count();

		List<ReviewAttempt> inWeek = s.attempts.stream().filter(r -> within(day(r), from, to)).toList();
		Judged answers = new Judged(inWeek.isEmpty() ? null : round1(avg(inWeek.stream().map(ReviewAttempt::getScore))),
				inWeek.size());

		Map<String, List<ReviewAttempt>> bySlug = s.attempts.stream()
				.collect(Collectors.groupingBy(ReviewAttempt::getQuestionSlug, LinkedHashMap::new, Collectors.toList()));
		List<Improved> improved = bySlug.entrySet().stream()
				.filter(e -> e.getValue().size() > 1 && within(day(last(e.getValue())), from, to))
				.map(e -> new Improved(e.getKey(), title(s, e.getKey()), e.getValue().get(0).getScore(), last(e.getValue()).getScore()))
				.filter(i -> i.to() > i.from())
				.sorted(Comparator.comparingDouble((Improved i) -> i.to() - i.from()).reversed())
				.limit(3)
				.toList();
		List<Scored> weakest = s.latestScore.entrySet().stream().filter(e -> e.getValue() < 7)
				.sorted(Map.Entry.comparingByValue())
				.limit(3)
				.map(e -> new Scored(e.getKey(), title(s, e.getKey()), e.getValue()))
				.toList();

		Set<LocalDate> active = new HashSet<>();
		s.lessonsDone.values().stream().filter(d -> within(d, from, to)).forEach(active::add);
		s.questionsDone.values().stream().filter(d -> within(d, from, to)).forEach(active::add);
		inWeek.forEach(r -> active.add(day(r)));

		return new Week(from, to, new Planned(lessonsDone, lessonsPlanned), new Planned(practiceDone, practicePlanned), answers,
				improved, weakest, active.size());
	}

	// ── Wins ──────────────────────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public List<Win> wins(int limit) {
		Snapshot s = snapshot(null);
		Map<String, AcademyModule> mods = s.lessonOrder.stream().collect(Collectors.toMap(AcademyModule::getId, Function.identity()));
		Stream<Win> lessons = s.lessonsDone.entrySet().stream().filter(e -> mods.containsKey(e.getKey()))
				.map(e -> new Win(e.getValue(), "LESSON", mods.get(e.getKey()).getCode() + ": " + mods.get(e.getKey()).getTitle(),
						"/academy/" + e.getKey()));
		Stream<Win> solved = s.questionsDone.entrySet().stream()
				.map(e -> new Win(e.getValue(), "QUESTION", title(s, e.getKey()), "/q/" + e.getKey()));
		Stream<Win> answers = s.attempts.stream().filter(r -> r.getScore() >= WIN_SCORE)
				.map(r -> new Win(day(r), "ANSWER", "Scored " + round1(r.getScore()) + "/10 on " + title(s, r.getQuestionSlug()),
						"/q/" + r.getQuestionSlug()));
		Stream<Win> written = stories.findAll().stream()
				.map(st -> new Win(LocalDate.ofInstant(st.getCreatedAt(), clock.getZone()), "STORY", "Story: " + st.getTitle(),
						"/progress/stories"));
		return Stream.of(lessons, solved, answers, written).flatMap(Function.identity())
				.sorted(Comparator.comparing(Win::date).reversed())
				.limit(limit)
				.toList();
	}

	// ── Shared ────────────────────────────────────────────────────────────────────────────────

	/** Everything the three views read, loaded once per request. */
	private record Snapshot(Map<String, Question> bySlug, List<AcademyModule> lessonOrder, Map<String, LocalDate> lessonsDone,
			Map<String, LocalDate> questionsDone, List<ReviewAttempt> attempts, Map<String, Double> latestScore,
			List<CampaignConfig.Session> sessions, List<String> planQuestions, int storyCount) {
	}

	private Snapshot snapshot(String campaignId) {
		Map<String, Question> bySlug = questions.findAll().stream().collect(Collectors.toMap(Question::getSlug, Function.identity()));
		List<String> path = paths.findById(MAIN_PATH).map(AcademyPath::getModules).map(InsightsService::split).orElse(List.of());
		Map<String, Integer> step = new LinkedHashMap<>();
		for (int i = 0; i < path.size(); i++) step.put(path.get(i), i);
		List<AcademyModule> lessonOrder = modules.findAll().stream()
				.sorted(Comparator.comparingInt((AcademyModule m) -> step.getOrDefault(m.getId(), Integer.MAX_VALUE))
						.thenComparingInt(AcademyModule::getOrdinal))
				.toList();
		Map<String, LocalDate> lessonsDone = lessonProgress.findAll().stream().filter(LessonProgress::isDone)
				.collect(Collectors.toMap(LessonProgress::getModuleId, p -> p.getDoneOn() == null ? LocalDate.now(clock) : p.getDoneOn()));
		Map<String, LocalDate> questionsDone = progress.findAll().stream().filter(Progress::isDone)
				.collect(Collectors.toMap(Progress::getQuestionSlug, p -> p.getDoneOn() == null ? LocalDate.now(clock) : p.getDoneOn()));
		List<ReviewAttempt> all = attempts.findAll().stream().sorted(Comparator.comparing(ReviewAttempt::getCreatedAt)).toList();
		Map<String, Double> latest = new LinkedHashMap<>();
		all.forEach(r -> latest.put(r.getQuestionSlug(), r.getScore()));
		List<CampaignConfig.Session> sessions = campaignId == null ? List.of()
				: campaigns.findById(campaignId).map(CampaignConfig::from).map(CampaignConfig::sessions).orElse(List.of());
		List<String> planQuestions = sessions.stream().sorted(Comparator.comparing(CampaignConfig.Session::date))
				.flatMap(x -> x.questions().stream()).filter(bySlug::containsKey).distinct().toList();
		return new Snapshot(bySlug, lessonOrder, lessonsDone, questionsDone, all, latest, sessions, planQuestions,
				(int) stories.count());
	}

	private static boolean inArea(Area a, Question q) {
		return q != null && a.types.contains(q.getType());
	}

	private static String title(Snapshot s, String slug) {
		Question q = s.bySlug.get(slug);
		return q == null ? slug : q.getTitle();
	}

	private LocalDate day(ReviewAttempt r) {
		return LocalDate.ofInstant(r.getCreatedAt(), clock.getZone());
	}

	private static boolean within(LocalDate d, LocalDate from, LocalDate to) {
		return d != null && !d.isBefore(from) && !d.isAfter(to);
	}

	private static <T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}

	private static double avg(Stream<Double> values) {
		return values.mapToDouble(Double::doubleValue).average().orElse(0);
	}

	private static double round1(double v) {
		return Math.round(v * 10) / 10.0;
	}

	private static List<String> split(String csv) {
		return csv == null || csv.isBlank() ? List.of() : Arrays.stream(csv.split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
	}
}
