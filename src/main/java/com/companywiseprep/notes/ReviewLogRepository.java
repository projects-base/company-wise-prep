package com.companywiseprep.notes;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

	/** When each grade was given, newest first: enough for the streak without loading whole rows. */
	@Query("select r.reviewedAt from ReviewLog r order by r.reviewedAt desc")
	List<Instant> reviewTimes();
}
