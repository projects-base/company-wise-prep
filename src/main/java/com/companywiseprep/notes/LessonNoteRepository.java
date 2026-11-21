package com.companywiseprep.notes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {

	List<LessonNote> findByModuleIdOrderByCreatedAtAsc(String moduleId);

	List<LessonNote> findByModuleIdAndKindOrderByIdAsc(String moduleId, NoteKind kind);
}
