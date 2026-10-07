package com.companywiseprep.progress;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.web.NotFoundException;

/**
 * Done / review / star / notes for a question.
 *
 * Reviews follow a fixed ladder of +3, +10, +30 days after solving. Remembering moves one rung
 * up (past the top the question retires from review); forgetting drops back to the bottom and
 * brings it back tomorrow.
 */
@Service
public class ProgressService {

	static final int[] LADDER_DAYS = {3, 10, 30};

	private final ProgressRepository progress;
	private final QuestionRepository questions;
	private final Clock clock;

	public ProgressService(ProgressRepository progress, QuestionRepository questions, Clock clock) {
		this.progress = progress;
		this.questions = questions;
		this.clock = clock;
	}

	public enum Action {
		DONE, UNDO, REMEMBERED, FORGOT, STAR, UNSTAR, NOTES
	}

	public record View(boolean done, LocalDate doneOn, int reviewStage, LocalDate nextReviewOn,
			boolean starred, String notes, String code) {

		public static final View EMPTY = new View(false, null, 0, null, false, null, null);

		static View of(Progress p) {
			return p == null ? EMPTY
					: new View(p.isDone(), p.getDoneOn(), p.getReviewStage(), p.getNextReviewOn(),
							p.isStarred(), p.getNotes(), p.getCode());
		}
	}

	/** Autosave from the code editor. Doesn't touch done/review state. */
	@Transactional
	public void saveCode(String slug, String code) {
		if (!questions.existsById(slug)) throw new NotFoundException("No question " + slug);
		Progress p = progress.findById(slug).orElseGet(() -> new Progress(slug));
		p.setCode(code);
		p.setUpdatedAt(Instant.now(clock));
		progress.save(p);
	}

	@Transactional
	public View apply(String slug, Action action, String notes) {
		if (!questions.existsById(slug)) throw new NotFoundException("No question " + slug);
		LocalDate today = LocalDate.now(clock);
		Progress p = progress.findById(slug).orElseGet(() -> new Progress(slug));
		switch (action) {
			case DONE -> {
				p.setDone(true);
				p.setDoneOn(today);
				p.setReviewStage(0);
				p.setNextReviewOn(today.plusDays(LADDER_DAYS[0]));
			}
			case UNDO -> {
				p.setDone(false);
				p.setDoneOn(null);
				p.setReviewStage(0);
				p.setNextReviewOn(null);
			}
			case REMEMBERED -> {
				int stage = p.getReviewStage() + 1;
				p.setReviewStage(stage);
				p.setNextReviewOn(stage < LADDER_DAYS.length ? today.plusDays(LADDER_DAYS[stage]) : null);
			}
			case FORGOT -> {
				p.setReviewStage(0);
				p.setNextReviewOn(today.plusDays(1));
			}
			case STAR -> p.setStarred(true);
			case UNSTAR -> p.setStarred(false);
			case NOTES -> p.setNotes(notes);
		}
		p.setUpdatedAt(Instant.now(clock));
		return View.of(progress.save(p));
	}

	@Transactional(readOnly = true)
	public Map<String, View> all() {
		return progress.findAll().stream()
				.collect(Collectors.toMap(Progress::getQuestionSlug, View::of));
	}

	@Transactional(readOnly = true)
	public View of(String slug) {
		return View.of(progress.findById(slug).orElse(null));
	}

	@Transactional(readOnly = true)
	public List<Progress> dueReviews(LocalDate day) {
		return progress.findByDoneTrueAndNextReviewOnLessThanEqualOrderByNextReviewOnAsc(day);
	}
}
