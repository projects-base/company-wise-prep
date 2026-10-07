package com.companywiseprep.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A runnable version of a bank question, imported from data/code/<slug>/ (docs/CODE-CHALLENGES.md). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CodeChallenge {

	/** Same slug as the bank question. */
	@Id
	private String slug;

	private String method;

	@Column(length = 1_000_000)
	private String problem;

	@Column(length = 1_000_000)
	private String starter;

	/** The hidden Main.java harness. */
	@Column(length = 1_000_000)
	private String harness;

	@Column(length = 1_000_000)
	private String reference;

	/** tests.yaml as written. */
	@Column(length = 10_000_000)
	private String tests;
}
