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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A flashcard for spaced repetition. One card per source (a highlight, a quiz mistake, a snippet or a saved explanation):
 * the unique constraint makes a repeated wrong answer or a double click land on the same card. The source id is not a
 * foreign key, so deleting the source keeps the card.
 */
@Entity
@Table(name = "review_card", uniqueConstraints = @UniqueConstraint(columnNames = {"sourceKind", "sourceId"}),
		indexes = {@Index(columnList = "due"), @Index(columnList = "moduleId")})
@Getter
@Setter
@NoArgsConstructor
public class ReviewCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private CardSource sourceKind;

	private Long sourceId;

	/** Null for a card from a general Playground snippet. */
	private String moduleId;

	@Column(length = 1_000)
	private String front;

	@Column(length = 4_000)
	private String back;

	private Instant due;

	/** Minutes until the card is due again after its last grade; 0 for a new card. */
	private long intervalMinutes;

	private double ease;

	private int reps;

	private int lapses;

	private boolean suspended;

	private Instant createdAt;

	private Instant updatedAt;

	/** Two grades of one card racing: the second fails instead of scheduling it twice. */
	@Version
	private long version;

	Scheduler.State state() {
		return new Scheduler.State(reps, lapses, ease, intervalMinutes);
	}

	void apply(Scheduler.Next next) {
		reps = next.state().reps();
		lapses = next.state().lapses();
		ease = next.state().ease();
		intervalMinutes = next.state().intervalMinutes();
		due = next.due();
	}
}
