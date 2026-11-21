package com.companywiseprep.notes;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A ```quiz question answered wrongly in a lesson. One row per quiz (lesson id + a hash of the question text): a
 * repeat wrong answer counts up, a later right answer marks it fixed, and a wrong one after that re-opens it.
 */
@Entity
@Table(name = "quiz_attempt_mistake", uniqueConstraints = @UniqueConstraint(columnNames = {"moduleId", "quizKey"}))
@Getter
@Setter
@NoArgsConstructor
public class QuizMistake {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String moduleId;

	/** SHA-256 of the normalised question text, first 16 hex characters. */
	@Column(length = 16)
	private String quizKey;

	@Column(length = 1_000)
	private String question;

	/** The latest wrong option picked. */
	@Column(length = 1_000)
	private String chosen;

	@Column(length = 1_000)
	private String correct;

	@Column(length = 4_000)
	private String why;

	/** easy, medium or hard (the quiz's own level), or null. */
	@Column(length = 10)
	private String level;

	private int wrongCount;

	private boolean fixed;

	private Instant firstWrongAt;

	private Instant lastWrongAt;

	private Instant fixedAt;
}
