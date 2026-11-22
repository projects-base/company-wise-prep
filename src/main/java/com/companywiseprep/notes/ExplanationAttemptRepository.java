package com.companywiseprep.notes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExplanationAttemptRepository extends JpaRepository<ExplanationAttempt, Long> {

	List<ExplanationAttempt> findByModuleIdOrderByCreatedAtDescIdDesc(String moduleId);

	List<ExplanationAttempt> findTop10ByModuleIdOrderByCreatedAtDescIdDesc(String moduleId);

	List<ExplanationAttempt> findBySavedAtNotNullOrderByCreatedAtAsc();
}
