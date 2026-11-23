package com.companywiseprep.campaign;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.companywiseprep.web.NotFoundException;

/** Plan session steps ticked off by hand, per campaign, day and session. */
@Service
public class PlanTickService {

	public static final int MAX_KEY = 300;
	public static final int MAX_STEP = 50;

	private final PlanTickRepository ticks;
	private final CampaignRepository campaigns;
	private final Clock clock;
	private final TransactionTemplate tx;

	public PlanTickService(PlanTickRepository ticks, CampaignRepository campaigns, Clock clock,
			PlatformTransactionManager transactions) {
		this.ticks = ticks;
		this.campaigns = campaigns;
		this.clock = clock;
		this.tx = new TransactionTemplate(transactions);
	}

	public record TickView(LocalDate date, String key, List<Integer> steps, Instant updatedAt) {

		static TickView of(PlanTick t) {
			List<Integer> steps = t.getSteps() == null || t.getSteps().isEmpty() ? List.of()
					: Arrays.stream(t.getSteps().split(",")).map(Integer::valueOf).toList();
			return new TickView(t.getTickDate(), t.getSessionKey(), steps, t.getUpdatedAt());
		}
	}

	@Transactional(readOnly = true)
	public List<TickView> list(String campaignId) {
		requireCampaign(campaignId);
		return ticks.findByCampaignIdOrderByTickDateAscSessionKeyAsc(campaignId).stream().map(TickView::of).toList();
	}

	/**
	 * Sets a session's ticked steps (replacing what was there), so repeating a call changes nothing. Two first ticks for one
	 * session can race (two tabs): the loser's insert hits the unique key, so it is retried once and finds the winner's row.
	 */
	public TickView set(String campaignId, LocalDate day, String title, Collection<Integer> steps) {
		requireCampaign(campaignId);
		String key = key(title);
		String csv = steps.stream().distinct().sorted().map(String::valueOf).collect(Collectors.joining(","));
		try {
			return tx.execute(status -> apply(campaignId, day, key, csv));
		} catch (DataIntegrityViolationException raced) {
			return tx.execute(status -> apply(campaignId, day, key, csv));
		}
	}

	private TickView apply(String campaignId, LocalDate day, String key, String csv) {
		PlanTick t = ticks.findByCampaignIdAndTickDateAndSessionKey(campaignId, day, key).orElseGet(() -> {
			PlanTick fresh = new PlanTick();
			fresh.setCampaignId(campaignId);
			fresh.setTickDate(day);
			fresh.setSessionKey(key);
			return fresh;
		});
		t.setSteps(csv);
		t.setUpdatedAt(Instant.now(clock));
		return TickView.of(ticks.saveAndFlush(t));
	}

	/** "Scratch · Step 8 of 136 · JAVA-FUND-4: Types" → "Scratch · JAVA-FUND-4: Types": the counter changes on regeneration. */
	public static String key(String title) {
		return title.replaceAll("Step \\d+ of \\d+ · ", "").strip();
	}

	private void requireCampaign(String campaignId) {
		if (!campaigns.existsById(campaignId)) throw new NotFoundException("No campaign " + campaignId);
	}
}
