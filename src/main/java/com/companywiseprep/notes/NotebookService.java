package com.companywiseprep.notes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyTrack;
import com.companywiseprep.notes.MistakeService.MistakeView;
import com.companywiseprep.notes.NoteService.NoteView;
import com.companywiseprep.notes.NoteService.Summary;
import com.companywiseprep.web.NotFoundException;

/** Reads across all notes: one lesson's panel, the My notes page, search and the cheat-sheet export. Writes nothing. */
@Service
public class NotebookService {

	static final int MAX_HITS = 100;
	/** Ten years of weekly plan; anything beyond is a typo or a probe. */
	static final int MAX_WEEK = 520;
	private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

	private final LessonNoteRepository notes;
	private final QuizMistakeRepository mistakes;
	private final MistakeService mistakeViews;
	private final LessonCatalog catalog;
	private final Clock clock;

	public NotebookService(LessonNoteRepository notes, QuizMistakeRepository mistakes, MistakeService mistakeViews,
			LessonCatalog catalog, Clock clock) {
		this.notes = notes;
		this.mistakes = mistakes;
		this.mistakeViews = mistakeViews;
		this.catalog = catalog;
		this.clock = clock;
	}

	/** Everything written for one lesson. {@code week} is the plan week it is first scheduled in, when known. */
	public record LessonNotes(String moduleId, String code, String title, String trackId, String trackTitle, Integer week,
			LocalDate weekFrom, LocalDate weekTo, Summary summary, List<NoteView> highlights, List<NoteView> snippets,
			List<MistakeView> mistakes) {
	}

	/** @param general Playground snippets saved outside any lesson */
	public record Overview(List<LessonNotes> lessons, List<NoteView> general) {
	}

	/** kind is HIGHLIGHT, SNIPPET, SUMMARY or MISTAKE. */
	public record Hit(String kind, long id, String moduleId, String moduleCode, String moduleTitle, String text, String link) {
	}

	public record SearchResult(List<Hit> hits, Map<String, Integer> counts) {
	}

	public record Export(String filename, String markdown) {
	}

	@Transactional(readOnly = true)
	public LessonNotes lesson(String moduleId) {
		LessonCatalog.Snapshot s = catalog.load(null);
		if (s.module(moduleId).isEmpty()) throw new NotFoundException("No module " + moduleId);
		return group(s, moduleId, notes.findByModuleIdOrderByCreatedAtAsc(moduleId),
				mistakeViews.views(mistakes.findAll().stream().filter(m -> moduleId.equals(m.getModuleId())).toList()));
	}

	@Transactional(readOnly = true)
	public Overview overview(String campaignId) {
		return overview(catalog.load(campaignId), id -> true);
	}

	private Overview overview(LessonCatalog.Snapshot s, Predicate<String> inScope) {
		List<LessonNote> all = notes.findAll().stream().sorted(Comparator.comparing(LessonNote::getCreatedAt)).toList();
		Map<String, List<LessonNote>> byLesson = all.stream().filter(n -> n.getModuleId() != null)
				.collect(Collectors.groupingBy(LessonNote::getModuleId));
		Map<String, List<MistakeView>> mistakesByLesson = mistakeViews.views(mistakes.findAll()).stream()
				.collect(Collectors.groupingBy(MistakeView::moduleId));
		List<LessonNotes> lessons = Stream.concat(byLesson.keySet().stream(), mistakesByLesson.keySet().stream()).distinct()
				.filter(id -> s.module(id).isPresent())
				.filter(inScope)
				.sorted(s.order())
				.map(id -> group(s, id, byLesson.getOrDefault(id, List.of()), mistakesByLesson.getOrDefault(id, List.of())))
				.toList();
		List<NoteView> general = all.stream().filter(n -> n.getModuleId() == null).map(NoteView::of).toList();
		return new Overview(lessons, general);
	}

	private static LessonNotes group(LessonCatalog.Snapshot s, String id, List<LessonNote> rows, List<MistakeView> mistakes) {
		AcademyModule m = s.module(id).orElseThrow();
		LessonCatalog.PlanWeek w = s.weekOf(id).orElse(null);
		// The oldest row is the summary, the same one NoteService.saveSummary updates.
		Summary summary = rows.stream().filter(n -> n.getKind() == NoteKind.SUMMARY).min(Comparator.comparing(LessonNote::getId))
				.map(NoteService::summary).orElse(null);
		return new LessonNotes(id, m.getCode(), m.getTitle(), m.getTrackId(), s.trackTitle(m), w == null ? null : w.number(),
				w == null ? null : w.from(), w == null ? null : w.to(), summary, of(rows, NoteKind.HIGHLIGHT),
				of(rows, NoteKind.SNIPPET), mistakes);
	}

	private static List<NoteView> of(List<LessonNote> rows, NoteKind kind) {
		return rows.stream().filter(n -> n.getKind() == kind).map(NoteView::of).toList();
	}

	// ── Search ────────────────────────────────────────────────────────────────────────────────

	/** Case-insensitive substring search over everything Akhil wrote. In memory: one user, a few thousand rows at most. */
	@Transactional(readOnly = true)
	public SearchResult search(String query) {
		String q = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
		if (q.length() < 2 || q.length() > 200) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search needs 2 to 200 characters");
		}
		LessonCatalog.Snapshot s = catalog.load(null);
		List<Hit> hits = new ArrayList<>();
		for (LessonNote n : notes.findAll().stream().sorted(Comparator.comparing(LessonNote::getUpdatedAt).reversed()).toList()) {
			String text = switch (n.getKind()) {
				case HIGHLIGHT -> matches(q, n.getQuote(), n.getNote(), n.getSection()) ? join(n.getQuote(), n.getNote()) : null;
				case SNIPPET -> matches(q, n.getCode(), n.getOutput(), n.getNote())
						? (n.getNote() != null ? n.getNote() : firstLine(n.getCode())) : null;
				case SUMMARY -> summaryHit(q, NoteService.summary(n));
			};
			if (text != null) hits.add(hit(s, n.getKind().name(), n.getId(), n.getModuleId(), text));
		}
		for (MistakeView m : mistakeViews.views(mistakes.findAll())) {
			if (matches(q, m.question(), m.chosen(), m.correct(), m.why())) {
				hits.add(hit(s, "MISTAKE", m.id(), m.moduleId(), m.question()));
			}
		}
		Map<String, Integer> counts = new LinkedHashMap<>();
		Stream.of("HIGHLIGHT", "SUMMARY", "SNIPPET", "MISTAKE").forEach(k -> counts.put(k, 0));
		hits.forEach(h -> counts.merge(h.kind(), 1, Integer::sum));
		return new SearchResult(hits.stream().limit(MAX_HITS).toList(), counts);
	}

	private static Hit hit(LessonCatalog.Snapshot s, String kind, long id, String moduleId, String text) {
		AcademyModule m = s.module(moduleId).orElse(null);
		return new Hit(kind, id, moduleId, m == null ? null : m.getCode(), m == null ? null : m.getTitle(), text,
				moduleId == null ? "/notes" : "/academy/" + moduleId);
	}

	private static String summaryHit(String q, Summary sum) {
		List<String> labels = CheatSheet.SUMMARY_LABELS;
		List<String> values = sum.values();
		for (int i = 0; i < values.size(); i++) {
			if (matches(q, values.get(i))) return labels.get(i) + ": " + values.get(i);
		}
		return null;
	}

	private static boolean matches(String q, String... fields) {
		return Stream.of(fields).filter(Objects::nonNull).anyMatch(f -> f.toLowerCase(Locale.ROOT).contains(q));
	}

	private static String join(String a, String b) {
		return b == null ? a : a + " — " + b;
	}

	private static String firstLine(String code) {
		return code.strip().lines().findFirst().orElse("");
	}

	// ── Export ────────────────────────────────────────────────────────────────────────────────

	/**
	 * A Markdown cheat sheet built only from Akhil's own notes, highlights, summaries and mistakes, ordered by the plan.
	 *
	 * @param scope lesson (id = module id), track (id = track id), week (id = plan week number, needs a campaign) or all
	 */
	@Transactional(readOnly = true)
	public Export export(String scope, String id, String campaignId) {
		LessonCatalog.Snapshot s = catalog.load(campaignId);
		String today = LocalDate.now(clock).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
		return switch (scope == null ? "" : scope) {
			case "lesson" -> {
				AcademyModule m = s.module(id).orElseThrow(() -> new NotFoundException("No module " + id));
				yield export(s, m.getCode() + " · " + m.getTitle(), m.getCode(), today, x -> x.equals(m.getId()), false);
			}
			case "track" -> {
				AcademyTrack t = id == null ? null : s.trackById().get(id);
				if (t == null) throw new NotFoundException("No track " + id);
				yield export(s, t.getTitle(), t.getCode(), today, x -> t.getId().equals(s.byId().get(x).getTrackId()), false);
			}
			case "week" -> {
				if (s.planStart() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A week needs a campaign");
				int n = weekNumber(id);
				LessonCatalog.PlanWeek w = s.week(n);
				Set<String> lessons = Set.copyOf(s.lessonsIn(n));
				String title = "Week " + n + " (" + w.from().format(DAY_MONTH) + " – " + w.to().format(DAY_MONTH) + ")";
				yield export(s, title, "week-" + n, today, lessons::contains, false);
			}
			case "all" -> export(s, "Everything", "all", today, x -> true, true);
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must be lesson, track, week or all");
		};
	}

	private Export export(LessonCatalog.Snapshot s, String title, String slug, String today, Predicate<String> inScope,
			boolean withGeneral) {
		Overview o = overview(s, inScope);
		String md = CheatSheet.render(title, today, o.lessons(), withGeneral ? o.general() : List.of());
		return new Export(filename(slug), md);
	}

	/** cheatsheet-&lt;slug&gt;.md with only a-z, 0-9 and single inner dashes, so any track or lesson code is a safe name. */
	static String filename(String slug) {
		String safe = slug.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		return "cheatsheet-" + (safe.isEmpty() ? "notes" : safe) + ".md";
	}

	private static int weekNumber(String id) {
		try {
			int n = Integer.parseInt(id == null ? "" : id.strip());
			if (n >= 1 && n <= MAX_WEEK) return n;
		} catch (NumberFormatException e) {
			// falls through to the 400 below
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be a plan week number from 1 to " + MAX_WEEK);
	}
}
