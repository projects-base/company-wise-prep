package com.companywiseprep.academy;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyModuleRepository extends JpaRepository<AcademyModule, String> {
	Optional<AcademyModule> findByCode(String code);
}
