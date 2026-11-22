package com.companywiseprep.notes;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCardRepository extends JpaRepository<ReviewCard, Long> {

	Optional<ReviewCard> findBySourceKindAndSourceId(CardSource sourceKind, Long sourceId);

	List<ReviewCard> findBySuspendedFalseAndDueLessThanEqualOrderByDueAscIdAsc(Instant now);

	List<ReviewCard> findBySuspendedFalse();

	List<ReviewCard> findBySourceKind(CardSource sourceKind);

	List<ReviewCard> findByModuleIdOrderByIdAsc(String moduleId);

	List<ReviewCard> findAllByOrderByIdAsc();
}
