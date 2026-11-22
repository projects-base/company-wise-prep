package com.companywiseprep.notes;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One grade of one card, for the streak and stats. Kept with the interval and ease it produced, so a better scheduler
 * (FSRS) could later be fitted to the history.
 */
@Entity
@Table(name = "review_log", indexes = @Index(columnList = "reviewedAt"))
@Getter
@NoArgsConstructor
public class ReviewLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long cardId;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Grade grade;

	private Instant reviewedAt;

	private long intervalMinutes;

	private double ease;

	public ReviewLog(Long cardId, Grade grade, Instant reviewedAt, long intervalMinutes, double ease) {
		this.cardId = cardId;
		this.grade = grade;
		this.reviewedAt = reviewedAt;
		this.intervalMinutes = intervalMinutes;
		this.ease = ease;
	}
}
