package com.companywiseprep.notes;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.review.AnswerReview;
import com.companywiseprep.review.ReviewEngine;
import com.companywiseprep.web.NotFoundException;

/**
 * "Explain it in your words" at the end of a lesson: judged by the app's reviewer ({@link ReviewEngine}) against the
 * lesson text, every attempt kept for the score trend, and the improved version saved to notes as a review card.
 */
@Service
public class ExplanationService {

	static final String PROMPT = "Explain in your words: ";
	/** The lesson text sent as the reference, at most; long lessons are cut, the start holds the core idea. */
	static final int REFERENCE_MAX = 12_000;

	private final ExplanationAttemptRepository attempts;
	private final AcademyModuleRepository modules;
	private final ReviewEngine engine;
	private final CardService cards;
	private final Clock clock;
	private final TransactionTemplate tx;

	public ExplanationService(ExplanationAttemptRepository attempts, AcademyModuleRepository modules, ReviewEngine engine,
			CardService cards, Clock clock, PlatformTransactionManager transactions) {
		this.attempts = attempts;
		this.modules = modules;
		this.engine = engine;
		this.cards = cards;
		this.clock = clock;
		this.tx = new TransactionTemplate(transactions);
	}

	/** @param id the stored attempt, for "Save to notes" */
	public record ExplanationResult(long id, AnswerReview review) {
	}

	public record ExplanationView(long id, String moduleId, Instant createdAt, String mode, double score, String text,
			String improved, boolean saved) {

		static ExplanationView of(ExplanationAttempt a) {
			return new ExplanationView(a.getId(), a.getModuleId(), a.getCreatedAt(), a.getMode(), a.getScore(), a.getText(),
					a.getImproved(), a.getSavedAt() != null);
		}
	}

	/** Judges an explanation. No transaction is held while the reviewer works; it can take many seconds. */
	public ExplanationResult check(String moduleId, String text) {
		String said = Text.trim(text);
		if (said == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank");
		AcademyModule m = modules.findById(moduleId).orElseThrow(() -> new NotFoundException("No module " + moduleId));
		AnswerReview review = engine.judge(new ReviewEngine.Brief(PROMPT + m.getTitle(), null, List.of(),
				reference(m.getLesson()), said));
		ExplanationAttempt a = new ExplanationAttempt();
		a.setModuleId(m.getId());
		a.setCreatedAt(Instant.now(clock));
		a.setMode(ReviewEngine.trendMode(review));
		a.setScore(ReviewEngine.trendScore(review));
		a.setText(said);
		String improved = Text.trim(review.improvedAnswer());
		a.setImproved(Text.cut(improved, CardService.BACK_MAX));
		ExplanationAttempt saved = attempts.save(a);
		return new ExplanationResult(saved.getId(), review.withHistory(history(m.getId())));
	}

	/** A lesson's attempts, newest first. */
	@Transactional(readOnly = true)
	public List<ExplanationView> list(String moduleId) {
		return attempts.findByModuleIdOrderByCreatedAtDescIdDesc(moduleId).stream().map(ExplanationView::of).toList();
	}

	/** Saves the improved version to notes and makes its card; saving again changes nothing. */
	public ExplanationView save(long id) {
		try {
			return tx.execute(status -> saveOnce(id));
		} catch (DataIntegrityViolationException raced) {
			return tx.execute(status -> saveOnce(id));
		}
	}

	private ExplanationView saveOnce(long id) {
		ExplanationAttempt a = attempts.findById(id).orElseThrow(() -> new NotFoundException("No explanation " + id));
		if (a.getImproved() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This review has no improved version to save");
		}
		if (a.getSavedAt() == null) a.setSavedAt(Instant.now(clock));
		String title = modules.findById(a.getModuleId()).map(AcademyModule::getTitle).orElse(a.getModuleId());
		cards.forExplanation(a, PROMPT + title);
		return ExplanationView.of(attempts.save(a));
	}

	private List<Double> history(String moduleId) {
		List<Double> scores = new ArrayList<>(attempts.findTop10ByModuleIdOrderByCreatedAtDescIdDesc(moduleId).stream()
				.map(ExplanationAttempt::getScore).toList());
		Collections.reverse(scores);
		return scores;
	}

	/** The lesson's Markdown without its quiz, animation and diagram blocks, capped at {@link #REFERENCE_MAX}. */
	static String reference(String lesson) {
		if (lesson == null) return "";
		String text = lesson.replaceAll("(?s)(`{3,})(quiz|anim|mermaid)\\b.*?\\1", "").replaceAll("\\n{3,}", "\n\n").strip();
		return Text.cut(text, REFERENCE_MAX);
	}
}
