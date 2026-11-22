package com.companywiseprep.web;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.notes.CardService;
import com.companywiseprep.notes.CardService.CardView;
import com.companywiseprep.notes.CardSource;
import com.companywiseprep.notes.Grade;
import com.companywiseprep.notes.ReviewService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Flashcards and the daily spaced-repetition review. */
@RestController
@RequestMapping("/api/review")
public class ReviewController {

	private final CardService cards;
	private final ReviewService review;

	public ReviewController(CardService cards, ReviewService review) {
		this.cards = cards;
		this.review = review;
	}

	/**
	 * @param sourceKind HIGHLIGHT or SNIPPET (mistake and explanation cards are made by the app)
	 * @param front required for a highlight; a snippet's defaults from its code
	 */
	public record CardRequest(
			@NotNull CardSource sourceKind,
			@NotNull Long sourceId,
			@Size(max = 1_000) String front,
			@Size(max = 4_000) String back) {
	}

	/** Makes the card for a highlight or snippet, or edits the one it already has. */
	@PostMapping("/cards")
	public CardView make(@Valid @RequestBody CardRequest req) {
		return cards.make(req.sourceKind(), req.sourceId(), req.front(), req.back());
	}

	/** All cards, or one lesson's (for the "Flashcard on" chips). */
	@GetMapping("/cards")
	public List<CardView> cards(@RequestParam(required = false) String moduleId) {
		return cards.list(moduleId);
	}

	public record EditRequest(
			@NotBlank @Size(max = 1_000) String front,
			@Size(max = 4_000) String back,
			boolean suspended) {
	}

	@PutMapping("/cards/{id}")
	public CardView edit(@PathVariable long id, @Valid @RequestBody EditRequest req) {
		return cards.edit(id, req.front(), req.back(), req.suspended());
	}

	@DeleteMapping("/cards/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		cards.delete(id);
	}

	/** Cards due now, the longest-waiting first. */
	@GetMapping("/queue")
	public List<CardView> queue() {
		return review.queue();
	}

	public record GradeRequest(@NotNull Grade grade) {
	}

	/** 409 when the card is not due (already graded, or suspended). */
	@PostMapping("/cards/{id}/grade")
	public CardView grade(@PathVariable long id, @Valid @RequestBody GradeRequest req) {
		return review.grade(id, req.grade());
	}

	/** @param zone the browser's IANA time zone (Asia/Kolkata), so "today" and the streak follow the viewer */
	@GetMapping("/stats")
	public ReviewService.Stats stats(@RequestParam(required = false) String zone) {
		return review.stats(zone(zone));
	}

	private static ZoneId zone(String id) {
		if (id == null || id.isBlank()) return null;
		try {
			return ZoneId.of(id);
		} catch (DateTimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown time zone " + id);
		}
	}
}
