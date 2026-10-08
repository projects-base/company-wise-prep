package com.companywiseprep.bank;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A question exists once; the companies that asked it hang off it as sightings. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {

	@Id
	private String slug;

	private String title;

	/** DSA | LLD | HLD | BEHAVIORAL | DOMAIN | JAVA | SPRING | SQL */
	private String type;

	/** easy | medium | hard */
	private String difficulty;

	/** Comma-separated. */
	@Column(length = 2000)
	private String tags;

	private String leetcode;

	@Column(length = 1_000_000)
	private String prompt;

	/** One follow-up per line. */
	@Column(length = 1_000_000)
	private String followUps;

	/** Comma-separated curriculum module ids. */
	private String academy;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("seenOn DESC")
	private List<Sighting> sightings = new ArrayList<>();

	public void addSighting(Sighting s) {
		s.setQuestion(this);
		sightings.add(s);
	}
}
