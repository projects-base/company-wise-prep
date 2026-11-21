package com.companywiseprep.notes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.academy.AcademyPath;
import com.companywiseprep.academy.AcademyPathRepository;
import com.companywiseprep.academy.AcademyTrack;
import com.companywiseprep.academy.AcademyTrackRepository;
import com.companywiseprep.campaign.CampaignConfig;
import com.companywiseprep.campaign.CampaignRepository;

/**
 * The lessons as notes need them: titles, tracks, plan order and plan weeks. Weeks are numbered the way the Plan page
 * numbers them (the first chunk runs from the plan's start to the day before the first Monday, then Monday to Sunday),
 * so "week 2" means the same dates everywhere.
 */
@Component
class LessonCatalog {

	static final String MAIN_PATH = "zero-to-pro";

	/** "Scratch · Step 12 of 119 · JAVA-JVM-2: …" or "Capstone · CAPSTONE-1: …" (the same rule as PlanPage.tsx). */
	private static final Pattern SESSION_LESSON =
			Pattern.compile("^(?:Scratch · Step \\d+ of \\d+|Capstone) · ([A-Z]+(?:-[A-Z]+)*-\\d+):");

	private final AcademyModuleRepository modules;
	private final AcademyTrackRepository tracks;
	private final AcademyPathRepository paths;
	private final CampaignRepository campaigns;

	LessonCatalog(AcademyModuleRepository modules, AcademyTrackRepository tracks, AcademyPathRepository paths,
			CampaignRepository campaigns) {
		this.modules = modules;
		this.tracks = tracks;
		this.paths = paths;
		this.campaigns = campaigns;
	}

	record PlanWeek(int number, LocalDate from, LocalDate to) {
	}

	/** Everything loaded once per request. {@code planStart} is null without a campaign. */
	record Snapshot(Map<String, AcademyModule> byId, Map<String, AcademyTrack> trackById, Map<String, Integer> pathStep,
			Map<String, LocalDate> firstPlanned, LocalDate planStart) {

		Optional<AcademyModule> module(String id) {
			return Optional.ofNullable(id).map(byId::get);
		}

		String trackTitle(AcademyModule m) {
			AcademyTrack t = trackById.get(m.getTrackId());
			return t == null ? m.getTrackId() : t.getTitle();
		}

		/** The plan's own order first (first session date), then the Zero to Pro path, then track and position. */
		Comparator<String> order() {
			return Comparator.comparing((String id) -> firstPlanned.getOrDefault(id, LocalDate.MAX))
					.thenComparingInt(id -> pathStep.getOrDefault(id, Integer.MAX_VALUE))
					.thenComparingInt(id -> module(id).map(m -> trackOrdinal(m)).orElse(Integer.MAX_VALUE))
					.thenComparingInt(id -> module(id).map(AcademyModule::getOrdinal).orElse(Integer.MAX_VALUE))
					.thenComparing(Function.identity());
		}

		private int trackOrdinal(AcademyModule m) {
			AcademyTrack t = trackById.get(m.getTrackId());
			return t == null ? Integer.MAX_VALUE : t.getOrdinal();
		}

		/** The plan week a lesson is first scheduled in, if it is in the plan at all. */
		Optional<PlanWeek> weekOf(String moduleId) {
			LocalDate d = firstPlanned.get(moduleId);
			return d == null || planStart == null ? Optional.empty() : Optional.of(week(numberOf(d)));
		}

		/** Lessons scheduled in a week, in plan order. */
		List<String> lessonsIn(int week) {
			return firstPlanned.keySet().stream().filter(id -> numberOf(firstPlanned.get(id)) == week).sorted(order()).toList();
		}

		PlanWeek week(int n) {
			LocalDate firstMonday = planStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
			if (firstMonday.equals(planStart)) {
				LocalDate from = planStart.plusWeeks(n - 1L);
				return new PlanWeek(n, from, from.plusDays(6));
			}
			if (n == 1) return new PlanWeek(1, planStart, firstMonday.minusDays(1));
			LocalDate from = firstMonday.plusWeeks(n - 2L);
			return new PlanWeek(n, from, from.plusDays(6));
		}

		private int numberOf(LocalDate d) {
			LocalDate firstMonday = planStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
			int offset = firstMonday.equals(planStart) ? 1 : 2;
			return d.isBefore(firstMonday) ? 1 : offset + (int) (ChronoUnit.DAYS.between(firstMonday, d) / 7);
		}
	}

	Snapshot load(String campaignId) {
		Map<String, AcademyModule> byId = modules.findAll().stream()
				.collect(Collectors.toMap(AcademyModule::getId, Function.identity()));
		Map<String, AcademyTrack> trackById = tracks.findAll().stream()
				.collect(Collectors.toMap(AcademyTrack::getId, Function.identity()));
		List<String> path = paths.findById(MAIN_PATH).map(AcademyPath::getModules).map(LessonCatalog::split).orElse(List.of());
		Map<String, Integer> step = new HashMap<>();
		for (int i = 0; i < path.size(); i++) step.putIfAbsent(path.get(i), i);

		Optional<CampaignConfig> campaign = Optional.ofNullable(campaignId).flatMap(campaigns::findById)
				.map(CampaignConfig::from);
		Map<String, String> idByCode = byId.values().stream()
				.collect(Collectors.toMap(AcademyModule::getCode, AcademyModule::getId, (a, b) -> a));
		Map<String, LocalDate> firstPlanned = new LinkedHashMap<>();
		campaign.map(CampaignConfig::sessions).orElse(List.of()).stream()
				.sorted(Comparator.comparing(CampaignConfig.Session::date))
				.forEach(s -> {
					Matcher m = SESSION_LESSON.matcher(s.title() == null ? "" : s.title());
					if (!m.find()) return;
					String code = m.group(1);
					String id = byId.containsKey(code) ? code : idByCode.get(code);
					if (id != null) firstPlanned.putIfAbsent(id, s.date());
				});
		return new Snapshot(byId, trackById, step, firstPlanned, campaign.map(CampaignConfig::start).orElse(null));
	}

	private static List<String> split(String csv) {
		return csv == null || csv.isBlank() ? List.of()
				: Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}
}
