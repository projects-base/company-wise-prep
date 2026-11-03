package com.companywiseprep.academy;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Keyed by module id so re-importing the curriculum never touches it. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class LessonProgress {

	@Id
	private String moduleId;

	private boolean done;

	private LocalDate doneOn;

	private Instant updatedAt;

	/** Titles of the lesson's "## " parts ticked off, one per line (titles survive edits better than positions). */
	@Column(length = 4000)
	private String partsDone;

	public LessonProgress(String moduleId) {
		this.moduleId = moduleId;
	}
}
