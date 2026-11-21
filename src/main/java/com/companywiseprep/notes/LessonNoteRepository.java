package com.companywiseprep.notes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {

	List<LessonNote> findByModuleIdOrderByCreatedAtAsc(String moduleId);

	Optional<LessonNote> findFirstByModuleIdAndKind(String moduleId, NoteKind kind);
}
