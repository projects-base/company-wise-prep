package com.companywiseprep.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.LessonProgress;
import com.companywiseprep.academy.LessonProgressRepository;
import com.companywiseprep.notes.ConceptIndex.Concept;
import com.companywiseprep.notes.ConceptIndex.Item;
import com.companywiseprep.notes.MistakeService.MistakeView;

/**
 * Linked concepts: [[Concept]] in highlight notes, snippet notes, summaries and explanations ({@link ConceptIndex}),
 * where each concept shows up in lessons and mistakes, and the Reference glossary terms of studied lessons that no note
 * links yet (gaps). Reads only.
 */
@Service
public class ConceptService {

	/** The Reference lesson whose tables are the glossary. */
	static final String GLOSSARY = "R1";
	static final int MAX_LESSONS = 30;
	private static final int EXCERPT = 160;

	private final LessonNoteRepository notes;
	private final ExplanationAttemptRepository explanations;
	private final QuizMistakeRepository mistakes;
	private final MistakeService mistakeViews;
	private final LessonProgressRepository progress;
	private final LessonCatalog catalog;

	public ConceptService(LessonNoteRepository notes, ExplanationAttemptRepository explanations, QuizMistakeRepository mistakes,
			MistakeService mistakeViews, LessonProgressRepository progress, LessonCatalog catalog) {
		this.notes = notes;
		this.explanations = explanations;
		this.mistakes = mistakes;
		this.mistakeViews = mistakeViews;
		this.progress = progress;
		this.catalog = catalog;
	}

	public record ConceptView(String name, int count) {
	}

	/** A glossary term taught in a lesson Akhil has finished, that none of his notes links. */
	public record Gap(String term, String moduleId, String moduleCode, String moduleTitle) {
	}

	public record Overview(List<ConceptView> concepts, List<Gap> gaps) {
	}

	/** kind is HIGHLIGHT, SNIPPET, SUMMARY or EXPLANATION. */
	public record ItemView(String kind, long id, String moduleId, String moduleCode, String moduleTitle, String text,
			String link) {
	}

	public record LessonMention(String moduleId, String code, String title, String excerpt) {
	}

	public record Detail(String name, int count, List<ItemView> items, List<LessonMention> lessons, List<MistakeView> mistakes,
			List<ConceptView> related) {
	}

	@Transactional(readOnly = true)
	public Overview overview() {
		LessonCatalog.Snapshot s = catalog.load(null);
		ConceptIndex index = ConceptIndex.of(items());
		return new Overview(views(index.concepts()), gaps(s, index.linked()));
	}

	@Transactional(readOnly = true)
	public Detail detail(String name) {
		String n = Text.trim(name);
		if (n == null || n.length() > ConceptIndex.NAME_MAX) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A concept name needs 1 to " + ConceptIndex.NAME_MAX
					+ " characters");
		}
		LessonCatalog.Snapshot s = catalog.load(null);
		ConceptIndex index = ConceptIndex.of(items());
		List<ItemView> items = index.itemsFor(n).stream().map(i -> view(s, i)).toList();
		String display = Optional.ofNullable(index.name(n)).orElse(n.replaceAll("\\s+", " "));
		List<MistakeView> mentionedIn = mistakeViews.views(mistakes.findAll()).stream()
				.filter(m -> ConceptIndex.mentions(m.question(), n)).toList();
		return new Detail(display, items.size(), items, lessons(s, n), mentionedIn, views(index.related(n)));
	}

	/** Every note text that can hold a link: one item per highlight, snippet, summary and lesson explanation (the latest). */
	private List<Item> items() {
		List<Item> out = new ArrayList<>();
		for (LessonNote n : notes.findAll().stream().sorted(Comparator.comparing(LessonNote::getId)).toList()) {
			List<String> texts = n.getKind() == NoteKind.SUMMARY ? NoteService.summary(n).values()
					: Collections.singletonList(n.getNote());
			out.add(new Item(n.getKind().name(), n.getId(), n.getModuleId(), texts));
		}
		Map<String, ExplanationAttempt> latest = new LinkedHashMap<>();
		explanations.findAll().stream().sorted(Comparator.comparing(ExplanationAttempt::getCreatedAt))
				.forEach(a -> latest.put(a.getModuleId(), a));
		latest.values().forEach(a -> out.add(new Item("EXPLANATION", a.getId(), a.getModuleId(), List.of(a.getText()))));
		return out;
	}

	private static ItemView view(LessonCatalog.Snapshot s, Item i) {
		AcademyModule m = s.module(i.moduleId()).orElse(null);
		String text = i.texts().stream().filter(Objects::nonNull).collect(Collectors.joining(" · "));
		return new ItemView(i.kind(), i.id(), i.moduleId(), m == null ? null : m.getCode(), m == null ? null : m.getTitle(), text,
				i.moduleId() == null ? "/notes" : "/academy/" + i.moduleId());
	}

	private static List<ConceptView> views(List<Concept> concepts) {
		return concepts.stream().map(c -> new ConceptView(c.name(), c.count())).toList();
	}

	/** Lessons whose text names the term as a whole word, in plan order, each with the line it first appears on. */
	private static List<LessonMention> lessons(LessonCatalog.Snapshot s, String term) {
		Pattern pattern = ConceptIndex.mentionPattern(term);
		return s.byId().values().stream()
				.filter(m -> m.getLesson() != null && !m.getId().equals(GLOSSARY))
				.map(m -> {
					Matcher hit = pattern.matcher(m.getLesson());
					if (!hit.find()) return null;
					return new LessonMention(m.getId(), m.getCode(), m.getTitle(), excerpt(m.getLesson(), hit.start()));
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(LessonMention::moduleId, s.order()))
				.limit(MAX_LESSONS)
				.toList();
	}

	private static String excerpt(String text, int at) {
		int from = text.lastIndexOf('\n', at) + 1;
		int to = text.indexOf('\n', at);
		String line = text.substring(from, to < 0 ? text.length() : to).strip();
		return line.length() <= EXCERPT ? line : line.substring(0, EXCERPT - 1) + "…";
	}

	/** Glossary terms not linked by any note, limited to lessons marked done; each points at the first such lesson. */
	private List<Gap> gaps(LessonCatalog.Snapshot s, Set<String> linked) {
		Set<String> studied = progress.findAll().stream().filter(LessonProgress::isDone).map(LessonProgress::getModuleId)
				.collect(Collectors.toSet());
		if (studied.isEmpty()) return List.of();
		Map<String, AcademyModule> byCode = s.byId().values().stream()
				.collect(Collectors.toMap(AcademyModule::getCode, Function.identity(), (a, b) -> a));
		String glossary = s.module(GLOSSARY).map(AcademyModule::getLesson).orElse(null);
		Map<String, Gap> gaps = new LinkedHashMap<>();
		for (ConceptIndex.Term t : ConceptIndex.glossary(glossary)) {
			if (t.keys().stream().anyMatch(linked::contains)) continue;
			t.codes().stream().map(byCode::get).filter(m -> m != null && studied.contains(m.getId())).findFirst()
					.ifPresent(m -> gaps.putIfAbsent(t.keys().get(0), new Gap(t.term(), m.getId(), m.getCode(), m.getTitle())));
		}
		return List.copyOf(gaps.values());
	}
}
