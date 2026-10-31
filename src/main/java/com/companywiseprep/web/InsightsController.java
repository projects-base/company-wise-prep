package com.companywiseprep.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.insights.InsightsService;
import com.companywiseprep.insights.StoryService;
import com.companywiseprep.insights.StoryService.Draft;
import com.companywiseprep.insights.StoryService.StoryView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The Progress page: readiness by area, this week against the plan, wins, and the story bank. */
@RestController
public class InsightsController {

	private final InsightsService insights;
	private final StoryService stories;

	public InsightsController(InsightsService insights, StoryService stories) {
		this.insights = insights;
		this.stories = stories;
	}

	@GetMapping("/api/insights/readiness")
	public InsightsService.Readiness readiness(@RequestParam(required = false) String campaign) {
		return insights.readiness(campaign);
	}

	@GetMapping("/api/insights/week")
	public InsightsService.Week week(@RequestParam(required = false) String campaign,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return insights.week(campaign, date);
	}

	@GetMapping("/api/insights/wins")
	public List<InsightsService.Win> wins(@RequestParam(defaultValue = "200") int limit) {
		return insights.wins(Math.max(1, Math.min(limit, 500)));
	}

	public record StoryRequest(
			@NotBlank @Size(max = 200) String title,
			@Size(max = 4_000) String situation,
			@Size(max = 4_000) String task,
			@Size(max = 4_000) String action,
			@Size(max = 4_000) String result,
			@Size(max = 4_000) String learned,
			@Size(max = 20) List<@Size(max = 120) String> questions) {

		Draft draft() {
			return new Draft(title, situation, task, action, result, learned, questions);
		}
	}

	@GetMapping("/api/stories")
	public List<StoryView> stories() {
		return stories.list();
	}

	@PostMapping("/api/stories")
	public StoryView create(@Valid @RequestBody StoryRequest req) {
		return stories.create(req.draft());
	}

	@PutMapping("/api/stories/{id}")
	public StoryView update(@PathVariable long id, @Valid @RequestBody StoryRequest req) {
		return stories.update(id, req.draft());
	}

	@DeleteMapping("/api/stories/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		stories.delete(id);
	}
}
