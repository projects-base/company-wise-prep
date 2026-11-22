package com.companywiseprep.notes;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.companywiseprep.academy.LessonProgress;

/** [[Concept]] links across notes: the concept list, a concept's page, related concepts and glossary gaps. */
class ConceptsTest extends StudyTestBase {

	private void studied(String moduleId) {
		LessonProgress p = new LessonProgress(moduleId);
		p.setDone(true);
		lessonProgress.save(p);
	}

	@Test
	void conceptsAreCountedPerItemAcrossEveryKindOfNote() throws Exception {
		highlight("J3", "Override both or neither", "[[HashMap]] loses objects; see [[equals and hashCode]]");
		highlight("J3", "Buckets", "[[ hashmap ]] buckets");
		send(put("/api/notes/lessons/J3/summary"), "{\"idea\":\"[[HashMap]] needs [[equals and hashCode]]\"}")
				.andExpect(status().isOk());
		send(post("/api/notes/snippets"), "{\"moduleId\":null,\"code\":\"new HashMap<>()\",\"note\":\"[[LRU cache]]\"}")
				.andExpect(status().isOk());

		mvc.perform(get("/api/concepts"))
				.andExpect(jsonPath("$.concepts[*].name", contains("HashMap", "equals and hashCode", "LRU cache")))
				.andExpect(jsonPath("$.concepts[0].count", is(3)))
				.andExpect(jsonPath("$.concepts[1].count", is(2)));
	}

	@Test
	void aConceptPageShowsMyItemsTheLessonsAndMistakesThatMentionItAndRelatedConcepts() throws Exception {
		highlight("J3", "Override both or neither", "[[HashMap]] with [[equals and hashCode]]");
		quiz("J3", "What does a mutable key do to a HashMap?", null, false).andExpect(status().isOk());
		quiz("J3", "What is a ConcurrentHashMap?", null, false).andExpect(status().isOk());

		mvc.perform(get("/api/concepts/detail").param("name", "hashMAP"))
				.andExpect(jsonPath("$.name", is("HashMap")))
				.andExpect(jsonPath("$.count", is(1)))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].kind", is("HIGHLIGHT")))
				.andExpect(jsonPath("$.items[0].moduleCode", is("JAVA-FUND-3")))
				.andExpect(jsonPath("$.items[0].link", is("/academy/J3")))
				.andExpect(jsonPath("$.lessons[*].moduleId", hasItem("J3")))
				.andExpect(jsonPath("$.mistakes[*].question", contains("What does a mutable key do to a HashMap?")))
				.andExpect(jsonPath("$.related[*].name", contains("equals and hashCode")));
	}

	@Test
	void anUnlinkedTermStillShowsWhereItIsTaught() throws Exception {
		mvc.perform(get("/api/concepts/detail").param("name", "HashMap"))
				.andExpect(jsonPath("$.count", is(0)))
				.andExpect(jsonPath("$.items", empty()))
				.andExpect(jsonPath("$.lessons[*].moduleId", hasItem("J3")));
		mvc.perform(get("/api/concepts/detail").param("name", " ")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/concepts/detail").param("name", "x".repeat(101))).andExpect(status().isBadRequest());
	}

	@Test
	void gapsAreGlossaryTermsOfStudiedLessonsWithNoLink() throws Exception {
		mvc.perform(get("/api/concepts")).andExpect(jsonPath("$.gaps", empty()));

		studied("J1");
		mvc.perform(get("/api/concepts"))
				.andExpect(jsonPath("$.gaps[*].term", hasItem("JVM (Java Virtual Machine)")))
				.andExpect(jsonPath("$.gaps[*].moduleId", everyItem(is("J1"))));

		highlight("J1", "The JVM runs bytecode", "the [[jvm]] and the [[JDK (Java Development Kit)]]");
		mvc.perform(get("/api/concepts"))
				.andExpect(jsonPath("$.gaps[*].term", not(hasItem("JVM (Java Virtual Machine)"))))
				.andExpect(jsonPath("$.gaps[*].term", not(hasItem("JDK (Java Development Kit)"))))
				.andExpect(jsonPath("$.gaps[*].term", hasItem("JRE (Java Runtime Environment)")));
	}

	@Test
	void relatedConceptsIgnoreCase() throws Exception {
		highlight("J3", "a", "[[HashMap]] [[Bucket]]");
		highlight("J3", "b", "[[hashmap]] [[bucket]] [[Treeify]]");
		mvc.perform(get("/api/concepts/detail").param("name", "HashMap"))
				.andExpect(jsonPath("$.related[*].name", containsInAnyOrder("Bucket", "Treeify")))
				.andExpect(jsonPath("$.related[0].name", is("Bucket")));
	}
}
