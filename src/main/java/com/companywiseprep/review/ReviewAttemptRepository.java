package com.companywiseprep.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAttemptRepository extends JpaRepository<ReviewAttempt, Long> {

	List<ReviewAttempt> findTop10ByQuestionSlugOrderByCreatedAtDesc(String questionSlug);
}
