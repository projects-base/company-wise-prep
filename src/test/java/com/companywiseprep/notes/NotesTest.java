package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Lesson notes: highlights, summaries, Playground snippets, the mistakes notebook, search and export. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:notes;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
class NotesTest {

	static final LocalDate DAY = LocalDate.of(2026, 11, 25);
	static final Instant NOON = DAY.atTime(12, 0).toInstant(ZoneOffset.UTC);
	static final String PLAN = "backend-2027-01";

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		Clock testClock() {
			return Clock.fixed(NOON, ZoneOffset.UTC);
		}
	}

	@Autowired
	MockMvc mvc;

	@Autowired
	LessonNoteRepository notes;

	@Autowired
	QuizMistakeRepository mistakes;

	@BeforeEach
	void reset() {
		notes.deleteAll();
		mistakes.deleteAll();
	}

	private ResultActions send(MockHttpServletRequestBuilder req, String body) throws Exception {
		return mvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private String highlight(String module, String section, String quote, String note) throws Exception {
		String body = "{\"moduleId\":\"%s\",\"section\":\"%s\",\"quote\":\"%s\",\"note\":%s}".formatted(module, section, quote,
				note == null ? "null" : "\"" + note + "\"");
		return send(post("/api/notes/highlights"), body).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");
	}

	private ResultActions quiz(String module, String question, String chosen, boolean right) throws Exception {
		String body = """
				{"moduleId":"%s","question":"%s","chosen":"%s","correct":"Variable-size window",
				 "why":"Positive values only, so the window shrinks monotonically.","level":"medium","right":%s}"""
				.formatted(module, question, chosen, right);
		return send(post("/api/notes/quiz-attempts"), body);
	}

	// ── Highlights ────────────────────────────────────────────────────────────────────────────

	@Test
	void highlightsAreSavedListedEditedAndDeleted() throws Exception {
		String id = highlight("J1", "The JVM", "The JVM runs bytecode, not Java source.", "Say: write once, run anywhere");

		mvc.perform(get("/api/notes/lessons/J1"))
				.andExpect(jsonPath("$.code", is("JAVA-FUND-1")))
				.andExpect(jsonPath("$.highlights", hasSize(1)))
				.andExpect(jsonPath("$.highlights[0].section", is("The JVM")))
				.andExpect(jsonPath("$.highlights[0].quote", is("The JVM runs bytecode, not Java source.")))
				.andExpect(jsonPath("$.highlights[0].note", is("Say: write once, run anywhere")))
				.andExpect(jsonPath("$.summary", nullValue()));

		send(put("/api/notes/" + id), "{\"note\":\"Bytecode is portable\"}").andExpect(status().isOk())
				.andExpect(jsonPath("$.note", is("Bytecode is portable")));
		mvc.perform(delete("/api/notes/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/api/notes/lessons/J1")).andExpect(jsonPath("$.highlights", hasSize(0)));
		mvc.perform(delete("/api/notes/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void aHighlightWithoutANoteIsFine() throws Exception {
		highlight("J1", "The JVM", "Bytecode", null);
		mvc.perform(get("/api/notes/lessons/J1")).andExpect(jsonPath("$.highlights[0].note", nullValue()));
	}

	@Test
	void aHighlightNeedsAQuoteAKnownLessonAndBoundedText() throws Exception {
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"section\":\"x\",\"quote\":\" \"}")
				.andExpect(status().isBadRequest());
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"quote\":\"" + "q".repeat(1_001) + "\"}")
				.andExpect(status().isBadRequest());
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"quote\":\"ok\",\"note\":\"" + "n".repeat(4_001) + "\"}")
				.andExpect(status().isBadRequest());
		send(post("/api/notes/highlights"), "{\"moduleId\":\"NOPE\",\"quote\":\"ok\"}").andExpect(status().isNotFound());
	}

	// ── Summary ───────────────────────────────────────────────────────────────────────────────

	@Test
	void theSummaryIsUpsertedOncePerLesson() throws Exception {
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"javac makes bytecode\",\"inside\":\"class loader, JIT\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.idea", is("javac makes bytecode")));
		send(put("/api/notes/lessons/J1/summary"),
				"{\"idea\":\"javac makes bytecode\",\"inside\":\"class loader, JIT\",\"trap\":\"JRE is not a compiler\","
						+ "\"line\":\"The JVM runs bytecode anywhere\"}")
				.andExpect(status().isOk());

		mvc.perform(get("/api/notes/lessons/J1"))
				.andExpect(jsonPath("$.summary.trap", is("JRE is not a compiler")))
				.andExpect(jsonPath("$.summary.line", is("The JVM runs bytecode anywhere")));
		assertThat(notes.count()).isEqualTo(1);
	}

	@Test
	void summaryFieldsAreBounded() throws Exception {
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"" + "i".repeat(2_001) + "\"}").andExpect(status().isBadRequest());
	}

	// ── Mistakes ──────────────────────────────────────────────────────────────────────────────

	@Test
	void aWrongAnswerIsSavedARepeatDoesNotDuplicateAndARightOneFixesIt() throws Exception {
		String q = "Which pattern fits longest subarray with sum at most S?";
		quiz("J1", q, "Prefix sum + hash map", false).andExpect(status().isOk())
				.andExpect(jsonPath("$.fixed", is(false)))
				.andExpect(jsonPath("$.wrongCount", is(1)));
		quiz("J1", q, "Binary search", false).andExpect(jsonPath("$.wrongCount", is(2)))
				.andExpect(jsonPath("$.chosen", is("Binary search")));
		assertThat(mistakes.count()).isEqualTo(1);

		mvc.perform(get("/api/notes/mistakes"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].moduleCode", is("JAVA-FUND-1")))
				.andExpect(jsonPath("$[0].trackId", is("J")))
				.andExpect(jsonPath("$[0].correct", is("Variable-size window")))
				.andExpect(jsonPath("$[0].fixed", is(false)));

		quiz("J1", q, "Variable-size window", true).andExpect(status().isOk()).andExpect(jsonPath("$.fixed", is(true)));
		mvc.perform(get("/api/notes/mistakes")).andExpect(jsonPath("$[0].fixed", is(true)))
				.andExpect(jsonPath("$[0].fixedAt", not(nullValue())));

		// Wrong again after the fix re-opens the same mistake rather than adding another.
		quiz("J1", q, "Binary search", false)
				.andExpect(jsonPath("$.fixed", is(false)))
				.andExpect(jsonPath("$.wrongCount", is(3)));
		assertThat(mistakes.count()).isEqualTo(1);
	}

	@Test
	void aRightAnswerWithNoMistakeStoresNothing() throws Exception {
		quiz("J1", "Fresh question?", "Variable-size window", true).andExpect(status().isNoContent());
		assertThat(mistakes.count()).isZero();
	}

	@Test
	void theSameQuestionInAnotherLessonIsAnotherMistake() throws Exception {
		quiz("J1", "Shared?", "x", false);
		quiz("S0", "Shared?", "x", false);
		assertThat(mistakes.count()).isEqualTo(2);
	}

	@Test
	void aQuizAttemptNeedsAQuestionAndAKnownLesson() throws Exception {
		send(post("/api/notes/quiz-attempts"), "{\"moduleId\":\"J1\",\"question\":\"\",\"chosen\":\"a\",\"right\":false}")
				.andExpect(status().isBadRequest());
		quiz("NOPE", "Q?", "a", false).andExpect(status().isNotFound());
	}

	// ── Playground snippets ───────────────────────────────────────────────────────────────────

	@Test
	void snippetsAttachToALessonOrToGeneralNotes() throws Exception {
		send(post("/api/notes/snippets"), """
				{"moduleId":"J3","code":"seen.contains(new BadKey(\\"42\\"))","output":"false",
				 "note":"Override both or neither"}""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.kind", is("SNIPPET")))
				.andExpect(jsonPath("$.moduleId", is("J3")));
		send(post("/api/notes/snippets"), "{\"code\":\"System.out.println(1);\",\"output\":\"1\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.moduleId", nullValue()));

		mvc.perform(get("/api/notes/lessons/J3")).andExpect(jsonPath("$.snippets[0].output", is("false")));
		mvc.perform(get("/api/notes").param("campaign", PLAN))
				.andExpect(jsonPath("$.general", hasSize(1)))
				.andExpect(jsonPath("$.lessons[0].moduleId", is("J3")));
	}

	@Test
	void aSnippetNeedsBoundedCode() throws Exception {
		send(post("/api/notes/snippets"), "{\"code\":\" \"}").andExpect(status().isBadRequest());
		send(post("/api/notes/snippets"), "{\"code\":\"" + "c".repeat(20_001) + "\"}").andExpect(status().isBadRequest());
	}

	// ── My notes ──────────────────────────────────────────────────────────────────────────────

	@Test
	void myNotesListsLessonsInPlanOrderWithTheirWeek() throws Exception {
		highlight("S13", "Keeping up", "Read release notes", null);
		highlight("J1", "The JVM", "Bytecode", null);
		highlight("S0", "The request", "A request is bytes on a socket", null);

		mvc.perform(get("/api/notes").param("campaign", PLAN))
				.andExpect(jsonPath("$.lessons[*].moduleId", contains("S0", "J1", "S13")))
				// The plan starts on Sun 22 Nov, so week 1 is that day and week 2 is Mon 23 – Sun 29 Nov (as on the Plan page).
				.andExpect(jsonPath("$.lessons[0].week", is(2)))
				.andExpect(jsonPath("$.lessons[0].weekFrom", is("2026-11-23")))
				.andExpect(jsonPath("$.lessons[0].weekTo", is("2026-11-29")))
				.andExpect(jsonPath("$.lessons[0].trackTitle", is("The Story of Backend")));
	}

	// ── Search ────────────────────────────────────────────────────────────────────────────────

	@Test
	void searchFindsEveryKindCaseInsensitively() throws Exception {
		highlight("J1", "The JVM", "The JIT compiles hot methods", "amortised warm-up");
		send(put("/api/notes/lessons/J3/summary"), "{\"trap\":\"Amortised cost hides a slow step\"}");
		send(post("/api/notes/snippets"), "{\"moduleId\":\"J3\",\"code\":\"// amortised O(1) add\",\"output\":\"ok\"}");
		quiz("S0", "What is amortised analysis?", "Worst case of one call", false);
		highlight("S0", "Other", "Unrelated text", null);

		mvc.perform(get("/api/notes/search").param("q", "AMORTISED"))
				.andExpect(jsonPath("$.hits", hasSize(4)))
				.andExpect(jsonPath("$.counts.HIGHLIGHT", is(1)))
				.andExpect(jsonPath("$.counts.SUMMARY", is(1)))
				.andExpect(jsonPath("$.counts.SNIPPET", is(1)))
				.andExpect(jsonPath("$.counts.MISTAKE", is(1)))
				.andExpect(jsonPath("$.hits[?(@.kind == 'MISTAKE')].link", contains("/academy/S0")));
	}

	@Test
	void searchNeedsAtLeastTwoCharacters() throws Exception {
		mvc.perform(get("/api/notes/search").param("q", "a")).andExpect(status().isBadRequest());
	}

	// ── Cheat-sheet export ────────────────────────────────────────────────────────────────────

	@Test
	void aLessonExportUsesOnlyMyOwnWords() throws Exception {
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"javac makes bytecode\",\"line\":\"The JVM runs it anywhere\"}");
		highlight("J1", "The JVM", "The JIT compiles hot methods", "Say: tiered compilation");
		send(post("/api/notes/snippets"), "{\"moduleId\":\"J1\",\"code\":\"javap -c Main\",\"output\":\"0: iconst_1\"}");
		quiz("J1", "Which tool runs bytecode?", "javac", false);

		mvc.perform(get("/api/notes/export").param("scope", "lesson").param("id", "J1"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/markdown"))
				.andExpect(header().string("Content-Disposition", containsString("cheatsheet-java-fund-1.md")))
				.andExpect(content().string(containsString("# Cheat sheet · JAVA-FUND-1")))
				.andExpect(content().string(containsString("**Idea:** javac makes bytecode")))
				.andExpect(content().string(containsString("**Interview line:** The JVM runs it anywhere")))
				.andExpect(content().string(containsString("> The JIT compiles hot methods")))
				.andExpect(content().string(containsString("Say: tiered compilation")))
				.andExpect(content().string(containsString("javap -c Main")))
				.andExpect(content().string(containsString("Which tool runs bytecode?")))
				.andExpect(content().string(not(containsString("How it works inside"))))
				// Nothing from the lesson text itself.
				.andExpect(content().string(not(containsString("from source file to running program\n\nA"))));
	}

	@Test
	void aWeekExportHoldsThatWeeksLessonsInPlanOrder() throws Exception {
		highlight("J1", "s", "Second lesson of the week", null);
		highlight("S0", "s", "First lesson of the week", null);
		highlight("S13", "s", "Months later", null);

		String md = mvc.perform(get("/api/notes/export").param("scope", "week").param("id", "2").param("campaign", PLAN))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		assertThat(md).contains("# Cheat sheet · Week 2").doesNotContain("Months later");
		assertThat(md.indexOf("First lesson of the week")).isLessThan(md.indexOf("Second lesson of the week"));
	}

	@Test
	void trackAndEverythingExports() throws Exception {
		highlight("J1", "s", "Java note", null);
		highlight("S0", "s", "Story note", null);
		send(post("/api/notes/snippets"), "{\"code\":\"general();\"}");

		String track = mvc.perform(get("/api/notes/export").param("scope", "track").param("id", "J"))
				.andReturn().getResponse().getContentAsString();
		assertThat(track).contains("# Cheat sheet · Java Fundamentals").contains("Java note").doesNotContain("Story note");

		String all = mvc.perform(get("/api/notes/export").param("scope", "all").param("campaign", PLAN))
				.andReturn().getResponse().getContentAsString();
		assertThat(all).contains("Java note").contains("Story note").contains("general();");
		assertThat(all.indexOf("Story note")).isLessThan(all.indexOf("Java note"));
	}

	@Test
	void anEmptyExportSaysSoAndABadScopeIsRejected() throws Exception {
		mvc.perform(get("/api/notes/export").param("scope", "all"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("No notes yet")));
		mvc.perform(get("/api/notes/export").param("scope", "company")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/notes/export").param("scope", "week").param("id", "2")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/notes/export").param("scope", "track").param("id", "NOPE")).andExpect(status().isNotFound());
	}

	@Test
	void aWeekOutsideTheCapIsRejected() throws Exception {
		mvc.perform(get("/api/notes/export").param("scope", "week").param("id", "521").param("campaign", PLAN))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/notes/export").param("scope", "week").param("id", "0").param("campaign", PLAN))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/notes/export").param("scope", "week").param("id", "520").param("campaign", PLAN))
				.andExpect(status().isOk());
	}

	@Test
	void exportFilenamesAreSlugSafe() {
		assertThat(NotebookService.filename("Weird Code/../x ")).isEqualTo("cheatsheet-weird-code-x.md");
		assertThat(NotebookService.filename("--JAVA_FUND-1--")).isEqualTo("cheatsheet-java-fund-1.md");
		assertThat(NotebookService.filename("ü/")).isEqualTo("cheatsheet-notes.md");
	}

	// ── Review fixes ──────────────────────────────────────────────────────────────────────────

	@Test
	void twoSummaryRowsForOneLessonCollapseToOneOnTheNextSave() throws Exception {
		for (String idea : new String[] {"first", "second"}) {
			LessonNote row = new LessonNote();
			row.setModuleId("J1");
			row.setKind(NoteKind.SUMMARY);
			row.setFields("{\"idea\":\"" + idea + "\"}");
			row.setCreatedAt(NOON);
			row.setUpdatedAt(NOON);
			notes.save(row);
		}
		// The oldest row is the one read.
		mvc.perform(get("/api/notes/lessons/J1")).andExpect(jsonPath("$.summary.idea", is("first")));

		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"merged\"}").andExpect(status().isOk());
		assertThat(notes.count()).isEqualTo(1);
		mvc.perform(get("/api/notes/lessons/J1")).andExpect(jsonPath("$.summary.idea", is("merged")));
	}

	@Test
	void anAllBlankSummaryDeletesTheRow() throws Exception {
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"x\"}").andExpect(status().isOk());
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"  \",\"trap\":\"\"}").andExpect(status().isOk());
		assertThat(notes.count()).isZero();
		mvc.perform(get("/api/notes/lessons/J1")).andExpect(jsonPath("$.summary", nullValue()));
	}

	@Test
	void aSummaryIsNotEditedThroughTheNoteEndpoint() throws Exception {
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"x\"}");
		long id = notes.findAll().get(0).getId();
		send(put("/api/notes/" + id), "{\"note\":\"y\"}").andExpect(status().isBadRequest());
	}

	@Test
	void editingOrDeletingAMissingNoteIs404() throws Exception {
		send(put("/api/notes/999999"), "{\"note\":\"y\"}").andExpect(status().isNotFound());
		mvc.perform(delete("/api/notes/999999")).andExpect(status().isNotFound());
	}

	@Test
	void nulCharactersAreStrippedBeforeSaving() throws Exception {
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"section\":\"S\\u0000\",\"quote\":\"by\\u0000tecode\","
				+ "\"note\":\"n\\u0000\",\"prefix\":\"p\\u0000\"}").andExpect(status().isOk());
		send(post("/api/notes/snippets"), "{\"code\":\"a\\u0000b\",\"output\":\"o\\u0000\"}").andExpect(status().isOk());
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"i\\u0000\"}").andExpect(status().isOk());
		quiz("J1", "Q\\u0000?", "c\\u0000", false).andExpect(status().isOk());

		assertThat(notes.findAll()).allSatisfy(n -> assertThat(String.join("|", String.valueOf(n.getSection()),
				String.valueOf(n.getQuote()), String.valueOf(n.getNote()), String.valueOf(n.getCode()),
				String.valueOf(n.getOutput()), String.valueOf(n.getFields()), String.valueOf(n.getPrefix())))
				.doesNotContain("\u0000"));
		assertThat(mistakes.findAll().get(0).getQuestion()).isEqualTo("Q?");
		assertThat(mistakes.findAll().get(0).getChosen()).isEqualTo("c");
	}

	@Test
	void aHighlightKeepsItsPrefixContext() throws Exception {
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"quote\":\"bytecode\",\"prefix\":\"The JVM runs\"}")
				.andExpect(jsonPath("$.prefix", is("The JVM runs")));
		send(post("/api/notes/highlights"), "{\"moduleId\":\"J1\",\"quote\":\"ok\",\"prefix\":\"" + "p".repeat(201) + "\"}")
				.andExpect(status().isBadRequest());
	}

	@Test
	void aSummaryThatSerialisesTooLongIsRejectedNotA500() throws Exception {
		// Control characters escape to six characters each in JSON: 4 × 2,000 of them is far past the column.
		String escaped = "\\u0001".repeat(2_000);
		send(put("/api/notes/lessons/J1/summary"), "{\"idea\":\"%1$s\",\"inside\":\"%1$s\",\"trap\":\"%1$s\",\"line\":\"%1$s\"}"
				.formatted(escaped)).andExpect(status().isBadRequest());
	}
}
