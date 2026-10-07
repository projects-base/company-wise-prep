package com.companywiseprep;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
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

/** Runs against the real data/ directory, so it also checks that every committed file imports. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:apptest;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
class AppTest {

	/** A Wednesday inside the Microsoft campaign. */
	static final LocalDate DAY = LocalDate.of(2026, 10, 7);

	static class MovableClock extends Clock {
		Instant now = DAY.atStartOfDay(ZoneOffset.UTC).toInstant();

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}

		void set(LocalDate day) {
			now = day.atStartOfDay(ZoneOffset.UTC).toInstant();
		}
	}

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		MovableClock testClock() {
			return new MovableClock();
		}
	}

	@Autowired
	MockMvc mvc;

	@Autowired
	MovableClock clock;

	@Autowired
	ProgressRepository progress;

	@Autowired
	LessonProgressRepository lessonProgress;

	@BeforeEach
	void reset() {
		progress.deleteAll();
		lessonProgress.deleteAll();
		clock.set(DAY);
	}

	private void act(String slug, String action) throws Exception {
		mvc.perform(put("/api/questions/" + slug + "/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"" + action + "\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void theResearchedBankImports() throws Exception {
		mvc.perform(get("/api/companies"))
				.andExpect(jsonPath("$[*].slug", hasItem("microsoft")))
				.andExpect(jsonPath("$[*].questions", everyItem(greaterThan(0))));
		mvc.perform(get("/api/questions")).andExpect(jsonPath("$", hasSize(greaterThan(300))));
		mvc.perform(get("/api/companies/microsoft"))
				.andExpect(jsonPath("$.data.official.job_postings", hasSize(greaterThan(0))))
				.andExpect(jsonPath("$.dossier", not(is(""))));
	}

	@Test
	void todayResumesTheQueueInsteadOfPilingUpMissedDays() throws Exception {
		String first = com.jayway.jsonpath.JsonPath.read(
				mvc.perform(get("/api/campaigns/microsoft-2027/today"))
						.andExpect(jsonPath("$.phase", is("STUDY")))
						.andExpect(jsonPath("$.items", hasSize(greaterThan(0))))
						.andReturn().getResponse().getContentAsString(),
				"$.items[0].slug");

		// A week passes with nothing done: today still offers the same first item, flagged as behind.
		clock.set(DAY.plusDays(7));
		mvc.perform(get("/api/campaigns/microsoft-2027/today"))
				.andExpect(jsonPath("$.items[*].slug", hasItem(first)))
				.andExpect(jsonPath("$.behind", greaterThan(0)));

		act(first, "DONE");
		mvc.perform(get("/api/campaigns/microsoft-2027/today"))
				.andExpect(jsonPath("$.items[*].slug", not(hasItem(first))))
				.andExpect(jsonPath("$.done", is(1)));
	}

	@Test
	void reviewsClimbTheLadderAndForgettingResetsIt() throws Exception {
		mvc.perform(put("/api/questions/lru-cache/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"DONE\"}"))
				.andExpect(jsonPath("$.nextReviewOn", is(DAY.plusDays(3).toString())));

		clock.set(DAY.plusDays(3));
		mvc.perform(get("/api/campaigns/microsoft-2027/today"))
				.andExpect(jsonPath("$.reviews[*].slug", hasItem("lru-cache")));
		mvc.perform(put("/api/questions/lru-cache/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"REMEMBERED\"}"))
				.andExpect(jsonPath("$.nextReviewOn", is(DAY.plusDays(13).toString())));
		mvc.perform(put("/api/questions/lru-cache/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"FORGOT\"}"))
				.andExpect(jsonPath("$.reviewStage", is(0)))
				.andExpect(jsonPath("$.nextReviewOn", is(DAY.plusDays(4).toString())));
	}

	@Test
	void reloadingTheBankKeepsProgress() throws Exception {
		act("lru-cache", "DONE");
		act("lru-cache", "STAR");
		mvc.perform(post("/api/admin/reload")).andExpect(jsonPath("$.questions", greaterThan(300)));
		mvc.perform(get("/api/questions/lru-cache"))
				.andExpect(jsonPath("$.progress.done", is(true)))
				.andExpect(jsonPath("$.progress.starred", is(true)))
				.andExpect(jsonPath("$.sightings", hasSize(greaterThan(1))));
	}

	@Test
	void unknownQuestionIs404() throws Exception {
		mvc.perform(get("/api/questions/no-such-question")).andExpect(status().isNotFound());
		mvc.perform(put("/api/questions/no-such-question/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"action\":\"DONE\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void theAcademyStartsFromScratchAndTracksProgress() throws Exception {
		mvc.perform(get("/api/academy"))
				.andExpect(jsonPath("$.paths[0].id", is("zero-to-pro")))
				.andExpect(jsonPath("$.paths[0].next", is("S0")))
				.andExpect(jsonPath("$.total", greaterThan(70)))
				.andExpect(jsonPath("$.tracks", hasSize(greaterThan(8))));

		mvc.perform(get("/api/academy/modules/A1"))
				.andExpect(jsonPath("$.module.ready", is(true)))
				.andExpect(jsonPath("$.previous.id", is("S0")))
				.andExpect(jsonPath("$.unlocks[*].id", hasItem("A2")));
		mvc.perform(get("/api/academy/modules/A2")).andExpect(jsonPath("$.module.ready", is(false)));

		mvc.perform(put("/api/academy/modules/A1/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{\"done\":true}"))
				.andExpect(jsonPath("$.done", is(true)));
		mvc.perform(get("/api/academy/modules/A2")).andExpect(jsonPath("$.module.ready", is(true)));
		mvc.perform(post("/api/admin/reload"));
		mvc.perform(get("/api/academy")).andExpect(jsonPath("$.done", is(1)));
		mvc.perform(get("/api/academy/modules/NOPE")).andExpect(status().isNotFound());
	}
}
