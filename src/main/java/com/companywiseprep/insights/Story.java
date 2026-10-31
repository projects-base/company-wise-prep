package com.companywiseprep.insights;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A behavioural story in STAR form, written by Akhil. Personal, so it lives only in the database,
 * never in data/ (the repo is public).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Story {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(length = 4_000)
	private String situation;

	@Column(length = 4_000)
	private String task;

	@Column(length = 4_000)
	private String action;

	@Column(length = 4_000)
	private String result;

	@Column(length = 4_000)
	private String learned;

	/** Comma-separated slugs of the behavioural questions this story answers. */
	@Column(length = 2_000)
	private String questions;

	private Instant createdAt;

	private Instant updatedAt;
}
