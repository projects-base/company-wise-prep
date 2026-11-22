package com.companywiseprep.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.notes.ConceptService;

/** Linked concepts: [[Concept]] in notes, where each shows up, related concepts and glossary gaps. */
@RestController
@RequestMapping("/api/concepts")
public class ConceptsController {

	private final ConceptService concepts;

	public ConceptsController(ConceptService concepts) {
		this.concepts = concepts;
	}

	@GetMapping
	public ConceptService.Overview overview() {
		return concepts.overview();
	}

	/** By query parameter, not path: a concept name may hold a slash ("equals / hashCode contract"). */
	@GetMapping("/detail")
	public ConceptService.Detail detail(@RequestParam String name) {
		return concepts.detail(name);
	}
}
