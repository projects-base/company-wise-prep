package com.companywiseprep.academy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One ~25-minute lesson. Defined in curriculum.yaml; the lesson body is lessons/<id>.md. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AcademyModule {

	@Id
	private String id;

	private String trackId;

	private String title;

	/** 1 Explorer … 5 Architect. */
	private int level;

	private int minutes;

	/** Comma-separated module ids. */
	private String prerequisites;

	/** Position within its track. */
	private int ordinal;

	/** Markdown; empty until the lesson is written. */
	@Column(length = 1_000_000)
	private String lesson;
}
