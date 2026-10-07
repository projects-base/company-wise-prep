package com.companywiseprep.academy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.web.NotFoundException;

@Service
public class AcademyService {

	/** The path the overview leads with and module pages step through. */
	static final String MAIN_PATH = "zero-to-pro";

	private final AcademyTrackRepository tracks;
	private final AcademyModuleRepository modules;
	private final AcademyPathRepository paths;
	private final LessonProgressRepository progress;
	private final Clock clock;

	public AcademyService(AcademyTrackRepository tracks, AcademyModuleRepository modules,
			AcademyPathRepository paths, LessonProgressRepository progress, Clock clock) {
		this.tracks = tracks;
		this.modules = modules;
		this.paths = paths;
		this.progress = progress;
		this.clock = clock;
	}

	/**
	 * @param ready every prerequisite is done — a suggestion, never a lock
	 */
	public record ModuleSummary(String id, String trackId, String title, int level, int minutes,
			List<String> prerequisites, boolean written, boolean done, boolean ready) {
	}

	public record TrackView(String id, String title, String description, List<ModuleSummary> modules) {
	}

	/** @param next the first module on the path that isn't done yet (null when finished) */
	public record PathView(String id, String title, String description, List<String> modules, int done,
			String next) {
	}

	public record Overview(List<PathView> paths, List<TrackView> tracks, int done, int total, int written) {
	}

	public record Ref(String id, String title, boolean done) {
	}

	public record ModuleView(ModuleSummary module, String trackTitle, String lesson, List<Ref> prerequisites,
			List<Ref> unlocks, Ref previous, Ref next, int pathPosition, int pathLength) {
	}

	@Transactional(readOnly = true)
	public Overview overview() {
		Set<String> done = doneIds();
		List<AcademyModule> all = modules.findAll();
		Map<String, List<ModuleSummary>> byTrack = all.stream()
				.sorted(Comparator.comparingInt(AcademyModule::getOrdinal))
				.map(m -> summary(m, done))
				.collect(Collectors.groupingBy(ModuleSummary::trackId));
		List<TrackView> trackViews = tracks.findAll().stream()
				.sorted(Comparator.comparingInt(AcademyTrack::getOrdinal))
				.map(t -> new TrackView(t.getId(), t.getTitle(), t.getDescription(),
						byTrack.getOrDefault(t.getId(), List.of())))
				.toList();
		List<PathView> pathViews = paths.findAll().stream()
				.sorted(Comparator.comparingInt(AcademyPath::getOrdinal))
				.map(p -> {
					List<String> ids = split(p.getModules());
					return new PathView(p.getId(), p.getTitle(), p.getDescription(), ids,
							(int) ids.stream().filter(done::contains).count(),
							ids.stream().filter(id -> !done.contains(id)).findFirst().orElse(null));
				})
				.toList();
		int written = (int) all.stream().filter(m -> !m.getLesson().isBlank()).count();
		return new Overview(pathViews, trackViews, (int) all.stream().filter(m -> done.contains(m.getId())).count(),
				all.size(), written);
	}

	@Transactional(readOnly = true)
	public ModuleView module(String id) {
		AcademyModule m = modules.findById(id).orElseThrow(() -> new NotFoundException("No module " + id));
		Set<String> done = doneIds();
		Map<String, AcademyModule> byId = modules.findAll().stream()
				.collect(Collectors.toMap(AcademyModule::getId, Function.identity()));
		Function<String, Ref> ref = x -> byId.containsKey(x) ? new Ref(x, byId.get(x).getTitle(), done.contains(x)) : null;

		List<String> path = paths.findById(MAIN_PATH).map(p -> split(p.getModules())).orElse(List.of());
		int pos = path.indexOf(id);
		Ref prev = pos > 0 ? ref.apply(path.get(pos - 1)) : null;
		Ref next = pos >= 0 && pos < path.size() - 1 ? ref.apply(path.get(pos + 1)) : null;

		List<Ref> unlocks = byId.values().stream()
				.filter(o -> split(o.getPrerequisites()).contains(id))
				.sorted(Comparator.comparing(AcademyModule::getId))
				.map(o -> ref.apply(o.getId()))
				.toList();
		String trackTitle = tracks.findById(m.getTrackId()).map(AcademyTrack::getTitle).orElse(m.getTrackId());
		return new ModuleView(summary(m, done), trackTitle, m.getLesson(),
				split(m.getPrerequisites()).stream().map(ref).filter(r -> r != null).toList(),
				unlocks, prev, next, pos + 1, path.size());
	}

	@Transactional
	public ModuleSummary setDone(String id, boolean isDone) {
		AcademyModule m = modules.findById(id).orElseThrow(() -> new NotFoundException("No module " + id));
		LessonProgress p = progress.findById(id).orElseGet(() -> new LessonProgress(id));
		p.setDone(isDone);
		p.setDoneOn(isDone ? LocalDate.now(clock) : null);
		p.setUpdatedAt(Instant.now(clock));
		progress.save(p);
		return summary(m, doneIds());
	}

	private ModuleSummary summary(AcademyModule m, Set<String> done) {
		List<String> pre = split(m.getPrerequisites());
		return new ModuleSummary(m.getId(), m.getTrackId(), m.getTitle(), m.getLevel(), m.getMinutes(), pre,
				!m.getLesson().isBlank(), done.contains(m.getId()), done.containsAll(pre));
	}

	private Set<String> doneIds() {
		return progress.findAll().stream().filter(LessonProgress::isDone).map(LessonProgress::getModuleId)
				.collect(Collectors.toSet());
	}

	private static List<String> split(String csv) {
		return csv == null || csv.isBlank() ? List.of()
				: Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}
}
