package com.companywiseprep.notes;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.web.NotFoundException;

/**
 * Review cards: made from a highlight or a snippet by hand, from every quiz mistake and every saved explanation by the
 * app; edited, suspended or deleted. One card per source, so making a card again edits the one there is.
 */
@Service
public class CardService {

	static final int FRONT_MAX = 1_000;
	static final int BACK_MAX = 4_000;
	static final String SNIPPET_FRONT = "What does this print, and why?";

	private final ReviewCardRepository cards;
	private final LessonNoteRepository notes;
	private final QuizMistakeRepository mistakes;
	private final AcademyModuleRepository modules;
	private final Clock clock;
	private final TransactionTemplate tx;

	public CardService(ReviewCardRepository cards, LessonNoteRepository notes, QuizMistakeRepository mistakes,
			AcademyModuleRepository modules, Clock clock, PlatformTransactionManager transactions) {
		this.cards = cards;
		this.notes = notes;
		this.mistakes = mistakes;
		this.modules = modules;
		this.clock = clock;
		this.tx = new TransactionTemplate(transactions);
	}

	/** @param next the interval in minutes each grade would give, for the labels on the Review buttons */
	public record CardView(long id, CardSource sourceKind, Long sourceId, String moduleId, String moduleCode,
			String moduleTitle, String front, String back, Instant due, long intervalMinutes, double ease, int reps,
			int lapses, boolean suspended, Instant createdAt, Instant updatedAt, Map<Grade, Long> next) {

		static CardView of(ReviewCard c, AcademyModule m) {
			return new CardView(c.getId(), c.getSourceKind(), c.getSourceId(), c.getModuleId(), m == null ? null : m.getCode(),
					m == null ? null : m.getTitle(), c.getFront(), c.getBack(), c.getDue(), c.getIntervalMinutes(), c.getEase(),
					c.getReps(), c.getLapses(), c.isSuspended(), c.getCreatedAt(), c.getUpdatedAt(),
					Scheduler.previews(c.state()));
		}
	}

	/**
	 * A card from a highlight or a snippet. A highlight card needs a front (the question Akhil writes); its back defaults
	 * to the quote and his note. A snippet card defaults both sides from the code, its output and note.
	 */
	public CardView make(CardSource kind, long sourceId, String front, String back) {
		if (kind != CardSource.HIGHLIGHT && kind != CardSource.SNIPPET) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a highlight or a snippet takes a hand-made card");
		}
		String f = Text.trim(front);
		String b = Text.trim(back);
		return raceSafe(() -> {
			LessonNote n = notes.findById(sourceId).orElseThrow(() -> new NotFoundException("No note " + sourceId));
			if (!n.getKind().name().equals(kind.name())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note " + sourceId + " is not a " + kind);
			}
			boolean highlight = kind == CardSource.HIGHLIGHT;
			if (highlight && f == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "front must not be blank");
			String frontText = f != null ? f : withCode(SNIPPET_FRONT, n.getCode());
			String myNote = n.getNote() == null ? null : "My note: " + n.getNote();
			String backText = b != null ? b : highlight ? joined(n.getQuote(), myNote)
					: Objects.requireNonNullElse(joined(n.getOutput(), n.getNote()), "No output was saved with this snippet.");
			ReviewCard c = cards.findBySourceKindAndSourceId(kind, sourceId)
					.orElseGet(() -> create(kind, sourceId, n.getModuleId()));
			c.setFront(fit(frontText, FRONT_MAX));
			c.setBack(fit(backText, BACK_MAX));
			c.setUpdatedAt(Instant.now(clock));
			return view(cards.save(c));
		});
	}

	/** The card for a quiz mistake, made on its first wrong answer; later ones leave it alone. In the caller's transaction. */
	void forMistake(QuizMistake q, String code) {
		if (cards.findBySourceKindAndSourceId(CardSource.MISTAKE, q.getId()).isPresent()) return;
		ReviewCard c = create(CardSource.MISTAKE, q.getId(), q.getModuleId());
		String lead = Objects.requireNonNullElse(q.getCorrect(), "(no correct option recorded)");
		c.setFront(code == null || code.isBlank() ? fit(q.getQuestion(), FRONT_MAX) : withCode(q.getQuestion(), code));
		c.setBack(fit(joined(lead, q.getWhy() == null ? null : "Why: " + q.getWhy()), BACK_MAX));
		cards.save(c);
	}

	/** The card for a saved explanation, made once. Runs in the caller's transaction. */
	void forExplanation(ExplanationAttempt a, String front) {
		if (cards.findBySourceKindAndSourceId(CardSource.EXPLANATION, a.getId()).isPresent()) return;
		ReviewCard c = create(CardSource.EXPLANATION, a.getId(), a.getModuleId());
		c.setFront(fit(front, FRONT_MAX));
		c.setBack(fit(a.getImproved(), BACK_MAX));
		cards.save(c);
	}

	/**
	 * Open mistakes recorded before cards existed (Phase 1) get their card. Idempotent: a mistake with a card is skipped,
	 * and two callers racing meet at the unique constraint.
	 */
	public void backfillMistakes() {
		raceSafe(() -> {
			Set<Long> carded = cards.findBySourceKind(CardSource.MISTAKE).stream().map(ReviewCard::getSourceId)
					.collect(Collectors.toSet());
			mistakes.findByFixedFalse().stream().filter(q -> !carded.contains(q.getId())).forEach(q -> forMistake(q, null));
			return null;
		});
	}

	public CardView edit(long id, String front, String back, boolean suspended) {
		String f = Text.trim(front);
		if (f == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "front must not be blank");
		return unraced(id, () -> {
			ReviewCard c = find(id);
			c.setFront(f);
			c.setBack(Text.trim(back));
			c.setSuspended(suspended);
			c.setUpdatedAt(Instant.now(clock));
			return view(cards.saveAndFlush(c));
		});
	}

	public void delete(long id) {
		unraced(id, () -> {
			cards.delete(find(id));
			cards.flush();
			return null;
		});
	}

	/** A change that meets a grade of the same card (the version column) is refused with 409, as a second grade is. */
	<T> T unraced(long id, Supplier<T> work) {
		try {
			return tx.execute(status -> work.get());
		} catch (OptimisticLockingFailureException raced) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Card " + id + " changed at the same time; try again");
		}
	}

	/** All cards, or one lesson's, oldest first. */
	@Transactional(readOnly = true)
	public List<CardView> list(String moduleId) {
		return views(moduleId == null || moduleId.isBlank() ? cards.findAllByOrderByIdAsc()
				: cards.findByModuleIdOrderByIdAsc(moduleId));
	}

	ReviewCard find(long id) {
		return cards.findById(id).orElseThrow(() -> new NotFoundException("No card " + id));
	}

	CardView view(ReviewCard c) {
		return views(List.of(c)).get(0);
	}

	List<CardView> views(List<ReviewCard> rows) {
		Map<String, AcademyModule> byId = modules.findAllById(rows.stream().map(ReviewCard::getModuleId).filter(Objects::nonNull)
				.distinct().toList()).stream().collect(Collectors.toMap(AcademyModule::getId, Function.identity()));
		return rows.stream().map(c -> CardView.of(c, c.getModuleId() == null ? null : byId.get(c.getModuleId()))).toList();
	}

	/** A new card is due at once. */
	private ReviewCard create(CardSource kind, long sourceId, String moduleId) {
		ReviewCard c = new ReviewCard();
		c.setSourceKind(kind);
		c.setSourceId(sourceId);
		c.setModuleId(moduleId);
		c.setEase(Scheduler.START_EASE);
		c.setCreatedAt(Instant.now(clock));
		c.setUpdatedAt(c.getCreatedAt());
		c.setDue(c.getCreatedAt());
		return c;
	}

	/** Two first "make card" clicks can race; the loser hits the unique constraint and its retry finds the winner's card. */
	private <T> T raceSafe(Supplier<T> work) {
		try {
			return tx.execute(status -> work.get());
		} catch (DataIntegrityViolationException raced) {
			return tx.execute(status -> work.get());
		}
	}

	/** The lead line, then the code in a java fence, the code shortened so the whole fits the front. */
	static String withCode(String lead, String code) {
		String head = lead + "\n\n```java\n";
		String tail = "\n```";
		int room = FRONT_MAX - head.length() - tail.length();
		if (code == null || code.isBlank() || room < 20) return fit(lead, FRONT_MAX);
		String c = code.strip();
		return head + (c.length() > room ? Text.cut(c, room - 1) + "…" : c) + tail;
	}

	private static String joined(String... parts) {
		String s = Stream.of(parts).filter(p -> p != null && !p.isBlank()).map(String::strip).collect(Collectors.joining("\n\n"));
		return s.isEmpty() ? null : s;
	}

	private static String fit(String s, int max) {
		return s == null || s.length() <= max ? s : Text.cut(s, max - 1) + "…";
	}
}
