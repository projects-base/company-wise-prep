package com.companywiseprep.academy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AcademyTrack {

	@Id
	private String id;

	private String title;

	@Column(length = 2000)
	private String description;

	private int ordinal;
}
