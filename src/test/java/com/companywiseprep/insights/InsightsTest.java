package com.companywiseprep.insights;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

import com.companywiseprep.academy.LessonProgressRepository;
import com.companywiseprep.progress.ProgressRepository;
import com.companywiseprep.review.ReviewAttempt;
import com.companywiseprep.review.ReviewAttemptRepository;

/** Readiness, the weekly report, the wins log and the story bank, against the real data/ directory. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:insights;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
class InsightsTest {

	/** Wednesday of week 1 of the backend plan (Mon 2 – Sun 8 Nov 2026). */
	static final LocalDate DAY = LocalDate.of(2026, 11, 4);
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
	ProgressRepository progress;

	@Autowired
	LessonProgressRepository lessons;

	@Autowired
	ReviewAttemptRepository attempts;

	@Autowired
	StoryRepository stories;

	@BeforeEach
	void reset() {
		progress.deleteAll();
		lessons.deleteAll();
		attempts.deleteAll();
		stories.deleteAll();
	}

	private void lessonDone(String id) throws Exception {
		mvc.perform(put("/api/academy/modules/" + id + "/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"done\":true}")).andExpect(status().isOk());
	}

	private void questionDone(String slug) throws Exception {
		mvc.perform(put("/api/questions/" + slug + "/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"DONE\"}")).andExpect(status().isOk());
	}

	@Test
	void readinessStartsAtZeroAndPointsAtTheFirstStep() throws Exception {
		mvc.perform(get("/api/insights/readiness").param("campaign", PLAN))
				.andExpect(jsonPath("$.areas[*].key", contains("dsa", "java", "spring", "sql", "lld", "hld", "behavioral")))
				.andExpect(jsonPath("$.areas[1].score", is(0)))
				.andExpect(jsonPath("$.areas[1].status", is("gap")))
				.andExpect(jsonPath("$.areas[1].next.link", is("/academy/J1")))
				.andExpect(jsonPath("$.areas[1].lessons.total", greaterThan(20)));
	}

	@Test
	void lessonsAndJudgedAnswersRaiseAnArea() throws Exception {
		lessonDone("J1");
		attempts.save(new ReviewAttempt("oop-four-pillars", NOON, "gemini", 8.5, "answer"));
		mvc.perform(get("/api/insights/readiness").param("campaign", PLAN))
				.andExpect(jsonPath("$.areas[1].lessons.done", is(1)))
				.andExpect(jsonPath("$.areas[1].judged.count", is(1)))
				.andExpect(jsonPath("$.areas[1].judged.average", is(8.5)))
				.andExpect(jsonPath("$.areas[1].score", greaterThan(0)))
				.andExpect(jsonPath("$.areas[1].next.kind", is("practice")))
				.andExpect(jsonPath("$.areas[1].next.link", startsWith("/q/")));
	}

	@Test
	void theWeekComparesWhatWasDoneWithThePlan() throws Exception {
		lessonDone("J1");
		questionDone("two-sum");
		attempts.save(new ReviewAttempt("jvm-internals", NOON.minusSeconds(86_400 * 10), "gemini", 4, "first"));
		attempts.save(new ReviewAttempt("jvm-internals", NOON, "gemini", 7, "better"));
		attempts.save(new ReviewAttempt("oop-four-pillars", NOON, "gemini", 3, "weak"));
		mvc.perform(get("/api/insights/week").param("campaign", PLAN))
				.andExpect(jsonPath("$.from", is("2026-11-02")))
				.andExpect(jsonPath("$.to", is("2026-11-08")))
				.andExpect(jsonPath("$.lessons.planned", is(9)))
				.andExpect(jsonPath("$.lessons.done", is(1)))
				.andExpect(jsonPath("$.practice.done", is(1)))
				.andExpect(jsonPath("$.practice.planned", greaterThan(4)))
				.andExpect(jsonPath("$.answers.count", is(2)))
				.andExpect(jsonPath("$.improved[0].slug", is("jvm-internals")))
				.andExpect(jsonPath("$.improved[0].from", is(4.0)))
				.andExpect(jsonPath("$.improved[0].to", is(7.0)))
				.andExpect(jsonPath("$.weakest[0].slug", is("oop-four-pillars")))
				.andExpect(jsonPath("$.activeDays", is(1)));
	}

	@Test
	void winsListEvidenceNewestFirst() throws Exception {
		lessonDone("J1");
		attempts.save(new ReviewAttempt("oop-four-pillars", NOON.minusSeconds(86_400 * 3), "gemini", 9, "great"));
		attempts.save(new ReviewAttempt("jvm-internals", NOON, "gemini", 5, "not a win"));
		mvc.perform(get("/api/insights/wins"))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].kind", is("LESSON")))
				.andExpect(jsonPath("$[0].link", is("/academy/J1")))
				.andExpect(jsonPath("$[1].kind", is("ANSWER")))
				.andExpect(jsonPath("$[1].date", is("2026-11-01")));
	}

	@Test
	void storiesAreCreatedEditedLinkedAndDeleted() throws Exception {
		String body = """
				{"title":"Cut a batch job from 6 h to 40 min","situation":"Nightly sync missed its window",
				 "task":"Own the fix","action":"Profiled, batched writes","result":"40 min, no misses since",
				 "learned":"Measure first","questions":["most-challenging-project"]}""";
		String id = mvc.perform(post("/api/stories").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Cut a batch job from 6 h to 40 min")))
				.andReturn().getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

		mvc.perform(get("/api/stories")).andExpect(jsonPath("$[0].questions", hasItem("most-challenging-project")));
		mvc.perform(get("/api/insights/readiness").param("campaign", PLAN))
				.andExpect(jsonPath("$.areas[6].stories", is(1)));
		mvc.perform(get("/api/insights/wins")).andExpect(jsonPath("$[0].kind", is("STORY")));

		mvc.perform(put("/api/stories/" + id).contentType(MediaType.APPLICATION_JSON)
				.content(body.replace("40 min, no misses since", "35 min")))
				.andExpect(jsonPath("$.result", is("35 min")));
		mvc.perform(delete("/api/stories/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/api/stories")).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(delete("/api/stories/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void aStoryNeedsATitle() throws Exception {
		mvc.perform(post("/api/stories").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
				.andExpect(status().isBadRequest());
	}
}
