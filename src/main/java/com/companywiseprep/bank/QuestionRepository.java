package com.companywiseprep.bank;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, String> {

	/** The whole bank is a few hundred rows; filtering happens in memory. */
	@Query("select distinct q from Question q left join fetch q.sightings")
	List<Question> findAllWithSightings();

	@Query("select q from Question q left join fetch q.sightings where q.slug = :slug")
	Optional<Question> findWithSightings(String slug);
}
