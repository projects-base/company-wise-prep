package com.companywiseprep.code;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CodeChallengeRepository extends JpaRepository<CodeChallenge, String> {

	@Query("select c.slug from CodeChallenge c")
	List<String> findAllSlugs();
}
