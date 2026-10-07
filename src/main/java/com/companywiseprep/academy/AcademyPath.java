package com.companywiseprep.academy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** An ordered route through modules across tracks, e.g. "Zero to Pro". */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AcademyPath {

	@Id
	private String id;

	private String title;

	@Column(length = 2000)
	private String description;

	/** Comma-separated module ids, in study order. */
	@Column(length = 2000)
	private String modules;

	private int ordinal;
}
