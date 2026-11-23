package com.companywiseprep.campaign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Plan step ticks kept on the server, so every origin (dev, local, hosted) sees the same checkboxes. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:plantick;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
class PlanTickTest {

	static final String PLAN = "backend-2027-01";
	static final String TICKS = "/api/campaigns/" + PLAN + "/ticks";

	@Autowired
	MockMvc mvc;

	@MockitoSpyBean
	PlanTickRepository ticks;

	@BeforeEach
	void clean() {
		reset(ticks);
		ticks.deleteAll();
	}

	ResultActions tick(String campaign, String date, String key, String steps) throws Exception {
		String body = "{\"date\":%s,\"key\":%s,\"steps\":%s}".formatted(date, key, steps);
		return mvc.perform(put("/api/campaigns/" + campaign + "/ticks").contentType(MediaType.APPLICATION_JSON).content(body));
	}

	@Test
	void theSameTickTwiceIsStoredOnce() throws Exception {
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0,2]").andExpect(status().isOk());
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0,2]")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.steps", contains(0, 2)));

		mvc.perform(get(TICKS))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].date", is("2026-11-24")))
				.andExpect(jsonPath("$[0].key", is("Mock interview")))
				.andExpect(jsonPath("$[0].steps", contains(0, 2)));
	}

	@Test
	void aTickReplacesTheStepsAndKeepsThemSortedAndDistinct() throws Exception {
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0,1]");
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[3,1,3]").andExpect(jsonPath("$.steps", contains(1, 3)));
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[]").andExpect(jsonPath("$.steps", hasSize(0)));

		mvc.perform(get(TICKS)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].steps", hasSize(0)));
	}

	/** The plan generator renumbers steps; "Step 8 of 136" must not orphan the ticks. */
	@Test
	void theKeySurvivesPlanRegeneration() throws Exception {
		tick(PLAN, "\"2026-11-24\"", "\"Scratch · Step 8 of 136 · JAVA-FUND-4: Types\"", "[0]")
				.andExpect(jsonPath("$.key", is("Scratch · JAVA-FUND-4: Types")));
		tick(PLAN, "\"2026-11-24\"", "\"Scratch · Step 9 of 140 · JAVA-FUND-4: Types\"", "[0,1]");

		mvc.perform(get(TICKS)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].steps", contains(0, 1)));
	}

	@Test
	void keysAreNormalised() {
		assertThat(PlanTickService.key("Scratch · Step 8 of 136 · JAVA-FUND-4: Types")).isEqualTo("Scratch · JAVA-FUND-4: Types");
		assertThat(PlanTickService.key("  Capstone · JAVA-CAP-1: Build it ")).isEqualTo("Capstone · JAVA-CAP-1: Build it");
		assertThat(PlanTickService.key("Say it out loud")).isEqualTo("Say it out loud");
	}

	@Test
	void ticksAreListedPerCampaign() throws Exception {
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0]");
		tick("microsoft-2027", "\"2026-11-24\"", "\"Mock interview\"", "[1]");

		mvc.perform(get(TICKS)).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].steps", contains(0)));
		mvc.perform(get("/api/campaigns/microsoft-2027/ticks")).andExpect(jsonPath("$[0].steps", contains(1)));
	}

	@Test
	void badTicksAreRejected() throws Exception {
		tick(PLAN, "\"2026-13-01\"", "\"Mock\"", "[0]").andExpect(status().isBadRequest());
		tick(PLAN, "null", "\"Mock\"", "[0]").andExpect(status().isBadRequest());
		tick(PLAN, "\"2026-11-24\"", "\" \"", "[0]").andExpect(status().isBadRequest());
		tick(PLAN, "\"2026-11-24\"", "\"" + "k".repeat(301) + "\"", "[0]").andExpect(status().isBadRequest());
		tick(PLAN, "\"2026-11-24\"", "\"Mock\"", "[51]").andExpect(status().isBadRequest());
		tick(PLAN, "\"2026-11-24\"", "\"Mock\"", "[-1]").andExpect(status().isBadRequest());
		tick(PLAN, "\"2026-11-24\"", "\"Mock\"", "null").andExpect(status().isBadRequest());
		tick("no-such-plan", "\"2026-11-24\"", "\"Mock\"", "[0]").andExpect(status().isNotFound());
		mvc.perform(get("/api/campaigns/no-such-plan/ticks")).andExpect(status().isNotFound());
		assertThat(ticks.count()).isZero();
	}

	/** Two tabs ticking the same new session: the loser's insert hits the unique key and is retried as an update. */
	@Test
	void aConcurrentFirstTickIsMergedIntoTheExistingRow() throws Exception {
		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0]");

		AtomicInteger calls = new AtomicInteger();
		doAnswer(inv -> calls.getAndIncrement() == 0 ? Optional.empty()
				: ticks.findAll().stream()
						.filter(t -> t.getCampaignId().equals(inv.getArgument(0)) && t.getTickDate().equals(inv.getArgument(1))
								&& t.getSessionKey().equals(inv.getArgument(2)))
						.findFirst())
				.when(ticks).findByCampaignIdAndTickDateAndSessionKey(anyString(), any(LocalDate.class), anyString());

		tick(PLAN, "\"2026-11-24\"", "\"Mock interview\"", "[0,1]")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.steps", contains(0, 1)));
		assertThat(ticks.count()).isEqualTo(1);
	}
}
