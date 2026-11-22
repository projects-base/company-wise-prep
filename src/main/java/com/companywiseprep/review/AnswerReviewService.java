package com.companywiseprep.review;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.web.NotFoundException;

/**
 * Judges a spoken or typed theory answer against the question's model answer (through {@link ReviewEngine}). Every
 * attempt is stored so the question can show its score trend.
 */
@Service
public class AnswerReviewService {

	private final QuestionRepository questions;
	private final ReviewAttemptRepository attempts;
	private final ReviewEngine engine;
	private final Clock clock;

	public AnswerReviewService(QuestionRepository questions, ReviewAttemptRepository attempts, ReviewEngine engine,
			Clock clock) {
		this.questions = questions;
		this.attempts = attempts;
		this.engine = engine;
		this.clock = clock;
	}

	@Transactional
	public AnswerReview review(String slug, String answer) {
		Question q = questions.findById(slug).orElseThrow(() -> new NotFoundException("No question " + slug));
		List<String> followUps = q.getFollowUps() == null || q.getFollowUps().isBlank() ? List.of()
				: Arrays.stream(q.getFollowUps().split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
		AnswerReview review = engine.judge(new ReviewEngine.Brief(q.getTitle(), q.getPrompt(), followUps, q.getAnswer(), answer));
		attempts.save(new ReviewAttempt(slug, Instant.now(clock), ReviewEngine.trendMode(review),
				ReviewEngine.trendScore(review), answer));
		return review.withHistory(history(slug));
	}

	@Transactional(readOnly = true)
	public List<Double> history(String slug) {
		List<Double> scores = new ArrayList<>(attempts.findTop10ByQuestionSlugOrderByCreatedAtDesc(slug).stream()
				.map(ReviewAttempt::getScore).toList());
		Collections.reverse(scores);
		return scores;
	}
}
