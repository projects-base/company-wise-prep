package com.companywiseprep.review;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One judged answer, kept so a question shows its score trend. Keyed by slug like Progress. */
@Entity
@Table(indexes = @Index(columnList = "questionSlug"))
@Getter
@NoArgsConstructor
public class ReviewAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String questionSlug;

	private Instant createdAt;

	private String mode;

	private double score;

	@Column(length = 20_000)
	private String answer;

	public ReviewAttempt(String questionSlug, Instant createdAt, String mode, double score, String answer) {
		this.questionSlug = questionSlug;
		this.createdAt = createdAt;
		this.mode = mode;
		this.score = score;
		this.answer = answer;
	}
}
