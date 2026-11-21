package com.companywiseprep.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.companywiseprep.notes.MistakeService;
import com.companywiseprep.notes.MistakeService.MistakeView;
import com.companywiseprep.notes.NoteService;
import com.companywiseprep.notes.NoteService.NoteView;
import com.companywiseprep.notes.NoteService.Summary;
import com.companywiseprep.notes.NotebookService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Lesson notes: highlights and margin notes, summaries, Playground snippets, the mistakes notebook, search, export. */
@RestController
@RequestMapping("/api/notes")
public class NotesController {

	private final NoteService notes;
	private final MistakeService mistakes;
	private final NotebookService notebook;

	public NotesController(NoteService notes, MistakeService mistakes, NotebookService notebook) {
		this.notes = notes;
		this.mistakes = mistakes;
		this.notebook = notebook;
	}

	/** Every lesson with notes, in plan order (weeks from the campaign, when given), plus general Playground snippets. */
	@GetMapping
	public NotebookService.Overview overview(@RequestParam(required = false) String campaign) {
		return notebook.overview(campaign);
	}

	@GetMapping("/lessons/{moduleId}")
	public NotebookService.LessonNotes lesson(@PathVariable String moduleId) {
		return notebook.lesson(moduleId);
	}

	public record HighlightRequest(
			@NotBlank @Size(max = 64) String moduleId,
			@Size(max = 300) String section,
			@NotBlank @Size(max = 1_000) String quote,
			@Size(max = 4_000) String note) {
	}

	@PostMapping("/highlights")
	public NoteView highlight(@Valid @RequestBody HighlightRequest req) {
		return notes.highlight(req.moduleId(), req.section(), req.quote(), req.note());
	}

	/** @param moduleId the lesson the Playground was opened from; null saves to general notes */
	public record SnippetRequest(
			@Size(max = 64) String moduleId,
			@NotBlank @Size(max = 20_000) String code,
			@Size(max = 20_000) String output,
			@Size(max = 4_000) String note) {
	}

	@PostMapping("/snippets")
	public NoteView snippet(@Valid @RequestBody SnippetRequest req) {
		return notes.snippet(req.moduleId(), req.code(), req.output(), req.note());
	}

	public record NoteRequest(@Size(max = 4_000) String note) {
	}

	@PutMapping("/{id}")
	public NoteView editNote(@PathVariable long id, @Valid @RequestBody NoteRequest req) {
		return notes.editNote(id, req.note());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		notes.delete(id);
	}

	public record SummaryRequest(
			@Size(max = 2_000) String idea,
			@Size(max = 2_000) String inside,
			@Size(max = 2_000) String trap,
			@Size(max = 2_000) String line) {
	}

	/** Autosaved from the lesson's notes panel; all four blank removes the summary. */
	@PutMapping("/lessons/{moduleId}/summary")
	public Summary summary(@PathVariable String moduleId, @Valid @RequestBody SummaryRequest req) {
		return notes.saveSummary(moduleId, new Summary(req.idea(), req.inside(), req.trap(), req.line(), null));
	}

	public record QuizAttemptRequest(
			@NotBlank @Size(max = 64) String moduleId,
			@NotBlank @Size(max = 1_000) String question,
			@Size(max = 1_000) String chosen,
			@Size(max = 1_000) String correct,
			@Size(max = 4_000) String why,
			@Pattern(regexp = "easy|medium|hard") String level,
			boolean right) {
	}

	/** A ```quiz answer on a lesson page: 200 with the mistake as it now stands, or 204 when there was nothing to record. */
	@PostMapping("/quiz-attempts")
	public ResponseEntity<MistakeView> quizAttempt(@Valid @RequestBody QuizAttemptRequest req) {
		return mistakes.record(new MistakeService.Attempt(req.moduleId(), req.question(), req.chosen(), req.correct(), req.why(),
				req.level(), req.right()))
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/mistakes")
	public List<MistakeView> mistakes() {
		return mistakes.list();
	}

	@GetMapping("/search")
	public NotebookService.SearchResult search(@RequestParam String q) {
		return notebook.search(q);
	}

	/** A Markdown cheat sheet: scope = lesson | track | week | all (id = module id, track id or week number). */
	@GetMapping("/export")
	public ResponseEntity<String> export(@RequestParam String scope, @RequestParam(required = false) String id,
			@RequestParam(required = false) String campaign) {
		NotebookService.Export e = notebook.export(scope, id, campaign);
		String disposition = ContentDisposition.attachment().filename(e.filename()).build().toString();
		return ResponseEntity.ok()
				.contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
				.body(e.markdown());
	}
}
