package com.companywiseprep.bank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One report of a question being asked at a company. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sighting {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Question question;

	/** Company slug. */
	private String company;

	private String role;

	private String round;

	/** "YYYY-MM" or "YYYY". */
	private String seenOn;

	/** claimed | verified */
	private String confidence;

	@Column(length = 2000)
	private String source;
}
