package com.companywiseprep.notes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.companywiseprep.academy.LessonProgressRepository;
import com.companywiseprep.review.ReviewEngine;
import com.companywiseprep.settings.SettingsService;
import com.companywiseprep.settings.SettingsService.Reviewer;

/**
 * Shared set-up for the Phase 2 tests (review cards, explanations, concepts), so they share one Spring context: a clock
 * the tests move, the offline reviewer (no API key is ever used), and empty notes before each test.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:study;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
@Import(StudyTestBase.Config.class)
abstract class StudyTestBase {

	static final LocalDate DAY = LocalDate.of(2026, 11, 25);
	static final Instant NOON = DAY.atTime(12, 0).toInstant(ZoneOffset.UTC);

	static class MovableClock extends Clock {
		Instant now = NOON;

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

		void advance(Duration d) {
			now = now.plus(d);
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
	LessonNoteRepository notes;

	@Autowired
	QuizMistakeRepository mistakes;

	@Autowired
	ReviewCardRepository cards;

	@Autowired
	ReviewLogRepository logs;

	@Autowired
	ExplanationAttemptRepository explanations;

	@Autowired
	LessonProgressRepository lessonProgress;

	@MockitoSpyBean
	SettingsService settings;

	@MockitoSpyBean
	ReviewEngine engine;

	@BeforeEach
	void resetAll() {
		cards.deleteAll();
		logs.deleteAll();
		explanations.deleteAll();
		notes.deleteAll();
		mistakes.deleteAll();
		lessonProgress.deleteAll();
		clock.now = NOON;
		reset(engine, settings);
		doReturn(Reviewer.OFFLINE).when(settings).effectiveReviewer();
	}

	ResultActions send(MockHttpServletRequestBuilder req, String body) throws Exception {
		return mvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	static String id(ResultActions r) throws Exception {
		return r.andReturn().getResponse().getContentAsString().replaceFirst("(?s)^\\{\"id\":(\\d+).*", "$1");
	}

	String highlight(String module, String quote, String note) throws Exception {
		String body = "{\"moduleId\":\"%s\",\"section\":\"S\",\"quote\":\"%s\",\"note\":%s}".formatted(module, quote,
				note == null ? "null" : "\"" + note + "\"");
		return id(send(post("/api/notes/highlights"), body).andExpect(status().isOk()));
	}

	ResultActions quiz(String module, String question, String code, boolean right) throws Exception {
		String body = """
				{"moduleId":"%s","question":"%s","code":%s,"chosen":"Fixed window","correct":"Prefix sums",
				 "why":"Negatives break the window.","level":"hard","right":%s}"""
				.formatted(module, question, code == null ? "null" : "\"" + code + "\"", right);
		return send(post("/api/notes/quiz-attempts"), body);
	}
}
