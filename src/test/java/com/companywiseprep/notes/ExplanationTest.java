package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.companywiseprep.review.AnswerReview;

/** "Explain it in your words": judged by the shared reviewer, stored per attempt, and saved to notes as a card. */
class ExplanationTest extends StudyTestBase {

	static final String TEXT = "The JVM runs bytecode. javac compiles source to class files and the JVM loads them with a "
			+ "class loader, interprets them and the JIT compiles hot code. In my project we used it on Linux servers.";

	private ResultActions explain(String module, String text) throws Exception {
		return send(post("/api/notes/lessons/" + module + "/explanations"), "{\"text\":\"" + text + "\"}");
	}

	private static AnswerReview reviewed(String improved) {
		List<AnswerReview.Layer> layers = List.of(new AnswerReview.Layer("what", "What it is", 8.0, "Clear."),
				new AnswerReview.Layer("how", "How it works", 6.0, "Order is off."),
				new AnswerReview.Layer("where", "Where you used it", 2.0, "Missing."),
				new AnswerReview.Layer("pitfalls", "What goes wrong", 7.0, "Good."));
		return new AnswerReview("claude", 6.5, "Close.", layers, List.of(), List.of(), List.of("JIT is not javac"), improved,
				null, null, List.of(), null);
	}

	@Test
	void withoutAnApiKeyTheOfflineCheckJudgesItAndEveryAttemptIsKept() throws Exception {
		explain("J1", TEXT).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.review.mode", is("offline")))
				.andExpect(jsonPath("$.review.layers", hasSize(4)))
				.andExpect(jsonPath("$.review.history", hasSize(1)));
		explain("J1", TEXT + " Bytecode is portable.").andExpect(status().isOk())
				.andExpect(jsonPath("$.review.history", hasSize(2)));

		mvc.perform(get("/api/notes/lessons/J1/explanations"))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].mode", is("offline")))
				.andExpect(jsonPath("$[0].text", startsWith("The JVM runs bytecode.")));
		assertThat(explanations.count()).isEqualTo(2);
	}

	@Test
	void theReviewerIsAskedAboutTheLessonWithItsTitleAndAReferenceWithoutQuizzes() throws Exception {
		explain("J1", TEXT).andExpect(status().isOk());
		verify(engine).judge(argThat(b ->
				b.title().startsWith("Explain in your words: ") && !b.reference().contains("```quiz")
						&& b.reference().length() <= ExplanationService.REFERENCE_MAX));
	}

	@Test
	void anExplanationIsBoundedAndNeedsAKnownLesson() throws Exception {
		explain("J1", " ").andExpect(status().isBadRequest());
		explain("J1", "x".repeat(6_001)).andExpect(status().isBadRequest());
		explain("NOPE", TEXT).andExpect(status().isNotFound());
		assertThat(explanations.count()).isZero();
	}

	@Test
	void savingTheImprovedVersionMakesANoteAndACardOnce() throws Exception {
		String improved = "The JVM loads class files, interprets bytecode and JIT-compiles hot paths.";
		doReturn(reviewed(improved)).when(engine).judge(any());
		String id = id(explain("J1", TEXT).andExpect(jsonPath("$.review.mode", is("claude"))));

		mvc.perform(post("/api/notes/explanations/" + id + "/save")).andExpect(status().isOk())
				.andExpect(jsonPath("$.saved", is(true)));
		mvc.perform(post("/api/notes/explanations/" + id + "/save")).andExpect(status().isOk());

		mvc.perform(get("/api/notes/lessons/J1"))
				.andExpect(jsonPath("$.explanations[*].improved", contains(improved)));
		mvc.perform(get("/api/review/cards"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].sourceKind", is("EXPLANATION")))
				.andExpect(jsonPath("$[0].front", startsWith("Explain in your words: ")));
		mvc.perform(get("/api/notes/search").param("q", "jit-compiles"))
				.andExpect(jsonPath("$.hits[0].kind", is("EXPLANATION")));
		mvc.perform(get("/api/notes/export").param("scope", "lesson").param("id", "J1"))
				.andExpect(content().string(containsString("### My explanation")));
	}

	@Test
	void anAttemptWithNoImprovedVersionCannotBeSaved() throws Exception {
		doReturn(reviewed(null)).when(engine).judge(any());
		String id = id(explain("J1", TEXT));
		mvc.perform(post("/api/notes/explanations/" + id + "/save")).andExpect(status().isBadRequest());
		mvc.perform(post("/api/notes/explanations/999999/save")).andExpect(status().isNotFound());
	}

	@Test
	void theReferenceDropsQuizAnimAndMermaidBlocksAndIsCapped() {
		String md = "## Idea\n\nThe **JVM** runs bytecode.\n\n```quiz\n{\"question\":\"Q\"}\n```\n\n```mermaid\ngraph TD\n```\n"
				+ "```anim\nx\n```\n\n```java\nint x = 1;\n```\n";
		String ref = ExplanationService.reference(md);
		assertThat(ref).contains("The **JVM** runs bytecode.").contains("int x = 1;")
				.doesNotContain("question").doesNotContain("graph TD").doesNotContain("```anim");
		assertThat(ExplanationService.reference("a".repeat(50_000))).hasSize(ExplanationService.REFERENCE_MAX);
		assertThat(ExplanationService.reference(null)).isEmpty();
	}
}
