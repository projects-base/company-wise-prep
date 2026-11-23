package com.companywiseprep.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.campaign.PlanTickService;
import com.companywiseprep.campaign.PlanTickService.TickView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The plan's session checkboxes, kept on the server so every origin shows the same ticks. */
@RestController
@RequestMapping("/api/campaigns/{id}/ticks")
public class PlanTickController {

	private final PlanTickService ticks;

	public PlanTickController(PlanTickService ticks) {
		this.ticks = ticks;
	}

	@GetMapping
	public List<TickView> list(@PathVariable String id) {
		return ticks.list(id);
	}

	/** @param key the session title; its "Step N of M · " counter is dropped, so the tick survives plan regeneration */
	public record TickRequest(@NotNull LocalDate date, @NotBlank @Size(max = PlanTickService.MAX_KEY) String key,
			@NotNull @Size(max = PlanTickService.MAX_STEP + 1)
			List<@NotNull @Min(0) @Max(PlanTickService.MAX_STEP) Integer> steps) {
	}

	/** Sets the ticked steps of one session on one day (idempotent: the list replaces what was there). */
	@PutMapping
	public TickView set(@PathVariable String id, @Valid @RequestBody TickRequest req) {
		return ticks.set(id, req.date(), req.key(), req.steps());
	}
}
