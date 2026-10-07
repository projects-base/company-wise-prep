package com.companywiseprep.progress;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What Akhil has done with a question. Keyed by slug, not by a foreign key, so
 * re-importing the bank from data/ never touches it.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Progress {

	@Id
	private String questionSlug;

	private boolean done;

	private LocalDate doneOn;

	/** Index into the review ladder; past its end the question is retired from review. */
	private int reviewStage;

	private LocalDate nextReviewOn;

	private boolean starred;

	@Column(length = 1_000_000)
	private String notes;

	/** The learner's latest code for the question's challenge (autosaved by the editor). */
	@Column(length = 1_000_000)
	private String code;

	private Instant updatedAt;

	public Progress(String questionSlug) {
		this.questionSlug = questionSlug;
	}
}
