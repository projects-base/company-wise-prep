package com.companywiseprep.progress;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, String> {

	List<Progress> findByDoneTrueAndNextReviewOnLessThanEqualOrderByNextReviewOnAsc(LocalDate day);
}
