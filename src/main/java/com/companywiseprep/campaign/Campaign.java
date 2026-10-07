package com.companywiseprep.campaign;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A preparation campaign, imported from data/campaigns/<id>/campaign.yaml. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Campaign {

	@Id
	private String id;

	private String name;

	@Column(length = 1_000_000)
	private String yaml;
}
