package com.companywiseprep.notes;

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
import lombok.Setter;

/**
 * One "Explain it in your words" attempt at the end of a lesson, kept so the lesson shows its score trend. When saved,
 * its improved version counts as a note (the lesson's notes panel, search, the cheat sheet) and gets a review card.
 */
@Entity
@Table(name = "explanation_attempt", indexes = @Index(columnList = "moduleId"))
@Getter
@Setter
@NoArgsConstructor
public class ExplanationAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String moduleId;

	private Instant createdAt;

	/** claude, gemini, offline or claude+gemini. */
	@Column(length = 20)
	private String mode;

	private double score;

	@Column(length = 6_000)
	private String text;

	/** The reviewer's improved explanation, if it gave one. */
	@Column(length = 4_000)
	private String improved;

	/** When "Save to notes" was pressed; null until then. */
	private Instant savedAt;
}
