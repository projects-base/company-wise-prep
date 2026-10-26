package com.companywiseprep.review;

import java.util.List;

/**
 * A judged theory answer. {@code mode} is "claude", "gemini" or "offline"; layers are scored 0-10 and a
 * null layer score means the offline check had nothing to compare for that layer.
 */
public record AnswerReview(
		String mode,
		double score,
		String verdict,
		List<Layer> layers,
		List<String> covered,
		List<String> missed,
		List<String> mistakes,
		String improvedAnswer,
		String followUp,
		String note,
		List<Double> history,
		/** Gemini's review when both reviewers are on; null otherwise. */
		AnswerReview second) {

	/** The four layers of a strong answer: what, how, where used, what goes wrong. */
	public record Layer(String key, String name, Double score, String comment) {
	}

	AnswerReview withHistory(List<Double> history) {
		return new AnswerReview(mode, score, verdict, layers, covered, missed, mistakes, improvedAnswer, followUp, note,
				history, second);
	}

	AnswerReview withSecond(AnswerReview second) {
		return new AnswerReview(mode, score, verdict, layers, covered, missed, mistakes, improvedAnswer, followUp, note,
				history, second);
	}

	AnswerReview withNote(String note) {
		return new AnswerReview(mode, score, verdict, layers, covered, missed, mistakes, improvedAnswer, followUp, note,
				history, second);
	}
}
