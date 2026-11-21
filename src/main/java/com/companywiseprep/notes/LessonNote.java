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
import lombok.Setter;

/**
 * Something Akhil wrote while studying a lesson. Keyed by module id, not a foreign key, so re-importing the
 * curriculum never touches it; personal, so it lives only in the database (the repo is public).
 *
 * A highlight anchors by its quote plus the section heading: lessons change rarely, and when the quote is gone the
 * highlight still shows in the notes panel, just unmarked in the text.
 */
@Entity
@Table(name = "lesson_note", indexes = @Index(columnList = "moduleId"))
@Getter
@Setter
@NoArgsConstructor
public class LessonNote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Null for a Playground snippet saved outside any lesson (the general notes bucket). */
	private String moduleId;

	@Enumerated(EnumType.STRING)
	private NoteKind kind;

	/** The "## " heading the highlight sits under. */
	@Column(length = 300)
	private String section;

	@Column(length = 1_000)
	private String quote;

	@Column(length = 4_000)
	private String note;

	@Column(length = 20_000)
	private String code;

	@Column(length = 20_000)
	private String output;

	/** SUMMARY only: the four fields as JSON, so later fields need no schema change. */
	@Column(length = 10_000)
	private String fields;

	private Instant createdAt;

	private Instant updatedAt;
}
