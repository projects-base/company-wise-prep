package com.companywiseprep.notes;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.web.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Highlights with margin notes, Playground snippets and the per-lesson summary. */
@Service
public class NoteService {

	private static final ObjectMapper JSON = new ObjectMapper();
	/** The summary's JSON column; four 2,000-character fields can escape past it (control characters become \\uXXXX). */
	static final int FIELDS_MAX = 20_000;

	private final LessonNoteRepository notes;
	private final AcademyModuleRepository modules;
	private final Clock clock;

	public NoteService(LessonNoteRepository notes, AcademyModuleRepository modules, Clock clock) {
		this.notes = notes;
		this.modules = modules;
		this.clock = clock;
	}

	/** @param prefix lesson text just before a highlight's quote, whitespace removed, to find the right occurrence */
	public record NoteView(long id, String moduleId, NoteKind kind, String section, String quote, String prefix, String note,
			String code, String output, Instant createdAt, Instant updatedAt) {

		static NoteView of(LessonNote n) {
			return new NoteView(n.getId(), n.getModuleId(), n.getKind(), n.getSection(), n.getQuote(), n.getPrefix(), n.getNote(),
					n.getCode(), n.getOutput(), n.getCreatedAt(), n.getUpdatedAt());
		}
	}

	/** The four summary fields: the idea, how it works inside, the trap, the interview line. */
	public record Summary(String idea, String inside, String trap, String line, Instant updatedAt) {

		static final List<String> FIELDS = List.of("idea", "inside", "trap", "line");

		List<String> values() {
			return Arrays.asList(idea, inside, trap, line);
		}
	}

	@Transactional
	public NoteView highlight(String moduleId, String section, String quote, String prefix, String note) {
		String q = Text.trim(quote);
		if (q == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quote must not be blank");
		LessonNote n = create(requireModule(moduleId), NoteKind.HIGHLIGHT);
		n.setSection(Text.trim(section));
		n.setQuote(q);
		n.setPrefix(Text.trim(prefix));
		n.setNote(Text.trim(note));
		return NoteView.of(notes.save(n));
	}

	/** A Playground snippet; without a lesson it goes to the general notes bucket. */
	@Transactional
	public NoteView snippet(String moduleId, String code, String output, String note) {
		LessonNote n = create(moduleId == null || moduleId.isBlank() ? null : requireModule(moduleId), NoteKind.SNIPPET);
		String c = Text.clean(code);
		if (c == null || c.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code must not be blank");
		String out = Text.clean(output);
		n.setCode(c);
		n.setOutput(out == null || out.isEmpty() ? null : out);
		n.setNote(Text.trim(note));
		return NoteView.of(notes.save(n));
	}

	/** Edits the margin note of a highlight or snippet. */
	@Transactional
	public NoteView editNote(long id, String note) {
		LessonNote n = find(id);
		if (n.getKind() == NoteKind.SUMMARY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Edit a summary by its lesson");
		}
		n.setNote(Text.trim(note));
		n.setUpdatedAt(Instant.now(clock));
		return NoteView.of(notes.save(n));
	}

	@Transactional
	public void delete(long id) {
		notes.delete(find(id));
	}

	/**
	 * Autosave: one summary row per lesson, replaced on every save; all four fields blank removes it. The oldest row is
	 * the summary (the one {@link NotebookService} reads); any extra row, e.g. from two first saves racing, is removed here.
	 */
	@Transactional
	public Summary saveSummary(String moduleId, Summary s) {
		String id = requireModule(moduleId);
		List<LessonNote> rows = notes.findByModuleIdAndKindOrderByIdAsc(id, NoteKind.SUMMARY);
		if (rows.size() > 1) notes.deleteAll(rows.subList(1, rows.size()));
		LessonNote row = rows.isEmpty() ? null : rows.get(0);
		Map<String, String> fields = new LinkedHashMap<>();
		List<String> values = s.values();
		for (int i = 0; i < Summary.FIELDS.size(); i++) fields.put(Summary.FIELDS.get(i), Text.trim(values.get(i)));
		if (fields.values().stream().allMatch(Objects::isNull)) {
			if (row != null) notes.delete(row);
			return new Summary(null, null, null, null, null);
		}
		String json = write(fields);
		if (json.length() > FIELDS_MAX) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The summary is too long");
		if (row == null) row = create(id, NoteKind.SUMMARY);
		row.setFields(json);
		row.setUpdatedAt(Instant.now(clock));
		return summary(notes.save(row));
	}

	static Summary summary(LessonNote row) {
		Map<String, String> f = read(row.getFields());
		return new Summary(f.get("idea"), f.get("inside"), f.get("trap"), f.get("line"), row.getUpdatedAt());
	}

	private LessonNote create(String moduleId, NoteKind kind) {
		LessonNote n = new LessonNote();
		n.setModuleId(moduleId);
		n.setKind(kind);
		n.setCreatedAt(Instant.now(clock));
		n.setUpdatedAt(n.getCreatedAt());
		return n;
	}

	private LessonNote find(long id) {
		return notes.findById(id).orElseThrow(() -> new NotFoundException("No note " + id));
	}

	private String requireModule(String moduleId) {
		if (moduleId == null || !modules.existsById(moduleId)) throw new NotFoundException("No module " + moduleId);
		return moduleId;
	}

	private static String write(Map<String, String> fields) {
		try {
			return JSON.writeValueAsString(fields);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Summary fields did not serialise", e);
		}
	}

	private static Map<String, String> read(String json) {
		if (json == null || json.isBlank()) return Map.of();
		try {
			return JSON.readValue(json, new TypeReference<Map<String, String>>() { });
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Stored summary is not valid JSON", e);
		}
	}
}
