package com.companywiseprep.campaign;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTickRepository extends JpaRepository<PlanTick, Long> {

	Optional<PlanTick> findByCampaignIdAndTickDateAndSessionKey(String campaignId, LocalDate day, String sessionKey);

	List<PlanTick> findByCampaignIdOrderByTickDateAscSessionKeyAsc(String campaignId);
}
