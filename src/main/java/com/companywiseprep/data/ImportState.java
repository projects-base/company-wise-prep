package com.companywiseprep.data;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fingerprint of data/ at the last import, so start-up can skip re-importing unchanged content. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ImportState {

	public static final String ID = "content";

	@Id
	private String id = ID;

	private String fingerprint;

	private Instant importedAt;
}
