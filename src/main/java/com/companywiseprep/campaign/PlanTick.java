package com.companywiseprep.campaign;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The steps ticked off in one plan session on one day. Kept on the server so every origin (dev server, local app, hosted
 * app) shows the same checkboxes. The session key is the title without its "Step N of M · " counter, which the plan
 * generator renumbers.
 */
@Entity
@Table(name = "plan_tick", uniqueConstraints = @UniqueConstraint(columnNames = {"campaignId", "tickDate", "sessionKey"}))
@Getter
@Setter
@NoArgsConstructor
public class PlanTick {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String campaignId;

	/** The plan day the session belongs to ("day" is reserved in H2). */
	private LocalDate tickDate;

	@Column(length = PlanTickService.MAX_KEY)
	private String sessionKey;

	/** Ticked step indices, ascending, comma-separated ("0,2,3"); empty when every tick was taken back. */
	@Column(length = 200)
	private String steps;

	private Instant updatedAt;
}
