package com.companywiseprep.bank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A researched company. The structured part stays as the YAML text it was imported from —
 * its shape (loop, official postings, sources) is read by the UI, not queried.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Company {

	@Id
	private String slug;

	private String name;

	private String researchedOn;

	@Column(length = 1_000_000)
	private String yaml;

	@Column(length = 1_000_000)
	private String dossier;
}
