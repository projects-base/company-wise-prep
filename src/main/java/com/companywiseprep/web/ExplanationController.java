package com.companywiseprep.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.notes.ExplanationService;
import com.companywiseprep.notes.ExplanationService.ExplanationResult;
import com.companywiseprep.notes.ExplanationService.ExplanationView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** "Explain it in your words" at the end of a lesson: judged by the app's reviewer, kept per attempt, saved to notes. */
@RestController
@RequestMapping("/api/notes")
public class ExplanationController {

	private final ExplanationService explanations;

	public ExplanationController(ExplanationService explanations) {
		this.explanations = explanations;
	}

	public record ExplainRequest(@NotBlank @Size(max = 6_000) String text) {
	}

	@PostMapping("/lessons/{moduleId}/explanations")
	public ExplanationResult check(@PathVariable String moduleId, @Valid @RequestBody ExplainRequest req) {
		return explanations.check(moduleId, req.text());
	}

	/** The lesson's attempts, newest first. */
	@GetMapping("/lessons/{moduleId}/explanations")
	public List<ExplanationView> list(@PathVariable String moduleId) {
		return explanations.list(moduleId);
	}

	/** Stores the improved version as a note and makes its review card; saving twice is harmless. */
	@PostMapping("/explanations/{id}/save")
	public ExplanationView save(@PathVariable long id) {
		return explanations.save(id);
	}
}
