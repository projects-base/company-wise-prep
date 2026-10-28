package com.companywiseprep.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface ImportStateRepository extends JpaRepository<ImportState, String> {

	/**
	 * SELECT ... FOR UPDATE on the state row: two app instances sharing one database (Render and
	 * Cloud Run on Neon) import one at a time; the second waits, then sees the new fingerprint.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from ImportState s where s.id = :id")
	Optional<ImportState> lockById(String id);
}
