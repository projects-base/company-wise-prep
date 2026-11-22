package com.companywiseprep.notes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizMistakeRepository extends JpaRepository<QuizMistake, Long> {

	Optional<QuizMistake> findByModuleIdAndQuizKey(String moduleId, String quizKey);

	List<QuizMistake> findByFixedFalse();
}
