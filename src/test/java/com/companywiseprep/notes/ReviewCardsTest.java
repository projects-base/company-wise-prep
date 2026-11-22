package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/** Flashcards from highlights, quiz mistakes and snippets; the daily queue, grading, and the review stats. */
class ReviewCardsTest extends StudyTestBase {

	private ResultActions card(String kind, String sourceId, String front, String back) throws Exception {
		String body = "{\"sourceKind\":\"%s\",\"sourceId\":%s,\"front\":%s,\"back\":%s}".formatted(kind, sourceId,
				front == null ? "null" : "\"" + front + "\"", back == null ? "null" : "\"" + back + "\"");
		return send(post("/api/review/cards"), body);
	}

	private String highlightCard(String front) throws Exception {
		String h = highlight("J1", "The JVM runs bytecode " + front, "portable");
		return id(card("HIGHLIGHT", h, front, "The JVM runs bytecode.").andExpect(status().isOk()));
	}

	private ResultActions grade(String cardId, String grade) throws Exception {
		return send(post("/api/review/cards/" + cardId + "/grade"), "{\"grade\":\"" + grade + "\"}");
	}

	// ── Making cards ──────────────────────────────────────────────────────────────────────────

	@Test
	void aHighlightBecomesACardDueNowAndMakingItAgainEditsTheSameCard() throws Exception {
		String h = highlight("J1", "The JVM runs bytecode, not Java source.", "Write once, run anywhere");
		card("HIGHLIGHT", h, "What does the JVM run?", null).andExpect(status().isOk())
				.andExpect(jsonPath("$.sourceKind", is("HIGHLIGHT")))
				.andExpect(jsonPath("$.moduleCode", is("JAVA-FUND-1")))
				.andExpect(jsonPath("$.back", is("The JVM runs bytecode, not Java source.\n\nMy note: Write once, run anywhere")))
				.andExpect(jsonPath("$.due", is(NOON.toString())))
				.andExpect(jsonPath("$.reps", is(0)));
		card("HIGHLIGHT", h, "What does the JVM execute?", "Bytecode").andExpect(status().isOk())
				.andExpect(jsonPath("$.front", is("What does the JVM execute?")));

		mvc.perform(get("/api/review/cards").param("moduleId", "J1"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].sourceId", is(Integer.parseInt(h))))
				.andExpect(jsonPath("$[0].back", is("Bytecode")));
	}

	@Test
	void deletingTheSourceKeepsTheCard() throws Exception {
		String h = highlight("J1", "Bytecode is portable", null);
		card("HIGHLIGHT", h, "Why is bytecode useful?", null).andExpect(status().isOk());
		mvc.perform(delete("/api/notes/" + h)).andExpect(status().isNoContent());
		mvc.perform(get("/api/review/cards")).andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void everyQuizMistakeMakesOneCardAndARepeatOrAFixDoesNotTouchIt() throws Exception {
		String q = "Why does a sliding window fail with negatives?";
		quiz("D1", q, "int[] a = {1, -1};", false).andExpect(status().isOk());
		quiz("D1", q, "int[] a = {1, -1};", false).andExpect(status().isOk());
		quiz("D1", q, "int[] a = {1, -1};", true).andExpect(status().isOk());

		mvc.perform(get("/api/review/cards"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].sourceKind", is("MISTAKE")))
				.andExpect(jsonPath("$[0].front", is(q + "\n\n```java\nint[] a = {1, -1};\n```")))
				.andExpect(jsonPath("$[0].back", is("Prefix sums\n\nWhy: Negatives break the window.")));
		assertThat(cards.count()).isEqualTo(1);
	}

	@Test
	void aRightAnswerWithNoMistakeMakesNoCard() throws Exception {
		quiz("D1", "Which pointer moves first?", null, true).andExpect(status().isNoContent());
		assertThat(cards.count()).isZero();
	}

	@Test
	void aSnippetCardIsBuiltFromItsCodeOutputAndNote() throws Exception {
		String code = "System.out.println(1 + 2 + \\\"3\\\");";
		String s = id(send(post("/api/notes/snippets"),
				"{\"moduleId\":\"J1\",\"code\":\"" + code + "\",\"output\":\"33\",\"note\":\"Left to right\"}")
				.andExpect(status().isOk()));
		card("SNIPPET", s, null, null).andExpect(status().isOk())
				.andExpect(jsonPath("$.front",
						is("What does this print, and why?\n\n```java\nSystem.out.println(1 + 2 + \"3\");\n```")))
				.andExpect(jsonPath("$.back", is("33\n\nLeft to right")));
	}

	@Test
	void cardInputIsValidated() throws Exception {
		String h = highlight("J1", "Bytecode", null);
		card("HIGHLIGHT", h, " ", null).andExpect(status().isBadRequest());
		card("HIGHLIGHT", h, "f".repeat(1_001), null).andExpect(status().isBadRequest());
		card("HIGHLIGHT", h, "ok", "b".repeat(4_001)).andExpect(status().isBadRequest());
		card("MISTAKE", "1", "ok", "ok").andExpect(status().isBadRequest());
		card("HIGHLIGHT", "999999", "ok", null).andExpect(status().isNotFound());
		card("HIGHLIGHT", "null", "ok", null).andExpect(status().isBadRequest());
		card("NOPE", h, "ok", null).andExpect(status().isBadRequest());
	}

	@Test
	void aCardCanBeEditedSuspendedAndDeleted() throws Exception {
		String c = highlightCard("What runs bytecode?");
		send(put("/api/review/cards/" + c), "{\"front\":\"Who runs bytecode?\",\"back\":\"The JVM\",\"suspended\":true}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.front", is("Who runs bytecode?")))
				.andExpect(jsonPath("$.suspended", is(true)));
		mvc.perform(get("/api/review/queue")).andExpect(jsonPath("$", hasSize(0)));
		send(put("/api/review/cards/" + c), "{\"front\":\"x\",\"back\":\"" + "b".repeat(4_001) + "\",\"suspended\":false}")
				.andExpect(status().isBadRequest());
		send(put("/api/review/cards/" + c), "{\"front\":\"\",\"back\":\"y\",\"suspended\":false}")
				.andExpect(status().isBadRequest());
		mvc.perform(delete("/api/review/cards/" + c)).andExpect(status().isNoContent());
		mvc.perform(delete("/api/review/cards/" + c)).andExpect(status().isNotFound());
	}

	// ── The queue and grading ─────────────────────────────────────────────────────────────────

	@Test
	void theQueueHoldsDueCardsOldestDueFirstWithTheNextIntervals() throws Exception {
		String a = highlightCard("A?");
		clock.advance(Duration.ofMinutes(1));
		String b = highlightCard("B?");
		clock.advance(Duration.ofMinutes(1));
		highlightCard("C?");

		mvc.perform(get("/api/review/queue"))
				.andExpect(jsonPath("$[*].front", contains("A?", "B?", "C?")))
				.andExpect(jsonPath("$[0].next.AGAIN", is(10)))
				.andExpect(jsonPath("$[0].next.GOOD", is(4 * 24 * 60)));

		grade(a, "GOOD").andExpect(status().isOk())
				.andExpect(jsonPath("$.intervalMinutes", is(4 * 24 * 60)))
				.andExpect(jsonPath("$.reps", is(1)));
		grade(b, "AGAIN").andExpect(status().isOk());
		mvc.perform(get("/api/review/queue")).andExpect(jsonPath("$[*].front", contains("C?")));

		clock.advance(Duration.ofMinutes(10));
		mvc.perform(get("/api/review/queue")).andExpect(jsonPath("$[*].front", contains("C?", "B?")));
		clock.advance(Duration.ofDays(4));
		mvc.perform(get("/api/review/queue")).andExpect(jsonPath("$[*].front", contains("C?", "B?", "A?")));
	}

	@Test
	void aCardThatIsNotDueCannotBeGradedTwice() throws Exception {
		String c = highlightCard("Q?");
		grade(c, "GOOD").andExpect(status().isOk());
		grade(c, "GOOD").andExpect(status().isConflict());
		assertThat(logs.count()).isEqualTo(1);
	}

	@Test
	void aGradeMustBeOneOfTheFour() throws Exception {
		String c = highlightCard("Q?");
		grade(c, "PERFECT").andExpect(status().isBadRequest());
		grade("999999", "GOOD").andExpect(status().isNotFound());
	}

	// ── Stats ─────────────────────────────────────────────────────────────────────────────────

	@Test
	void theStreakCountsConsecutiveDaysEndingTodayOrYesterday() throws Exception {
		String a = highlightCard("A?");
		String b = highlightCard("B?");
		String c = highlightCard("C?");
		grade(a, "AGAIN");
		clock.advance(Duration.ofDays(1));
		grade(a, "AGAIN");
		clock.advance(Duration.ofDays(1));
		grade(b, "AGAIN");
		mvc.perform(get("/api/review/stats"))
				.andExpect(jsonPath("$.streak", is(3)))
				.andExpect(jsonPath("$.reviewedToday", is(true)));

		clock.advance(Duration.ofDays(1));
		mvc.perform(get("/api/review/stats"))
				.andExpect(jsonPath("$.streak", is(3)))
				.andExpect(jsonPath("$.reviewedToday", is(false)));

		clock.advance(Duration.ofDays(1));
		mvc.perform(get("/api/review/stats")).andExpect(jsonPath("$.streak", is(0)));
		grade(c, "GOOD");
		mvc.perform(get("/api/review/stats")).andExpect(jsonPath("$.streak", is(1)));
	}

	@Test
	void statsCountCardsKnownWellAndDueCountsForTheNextSevenDays() throws Exception {
		String a = highlightCard("A?");
		String b = highlightCard("B?");
		highlightCard("C?");
		String d = highlightCard("D?");
		ReviewCard known = cards.findById(Long.parseLong(a)).orElseThrow();
		known.setIntervalMinutes(21 * 24 * 60);
		known.setReps(3);
		known.setDue(NOON.plus(Duration.ofDays(2)));
		cards.save(known);
		grade(b, "GOOD");
		send(put("/api/review/cards/" + d), "{\"front\":\"D?\",\"back\":\"x\",\"suspended\":true}").andExpect(status().isOk());

		mvc.perform(get("/api/review/stats"))
				.andExpect(jsonPath("$.dueNow", is(1)))
				.andExpect(jsonPath("$.knownWell", is(1)))
				.andExpect(jsonPath("$.active", is(3)))
				.andExpect(jsonPath("$.week", hasSize(7)))
				.andExpect(jsonPath("$.week[0].date", is(DAY.toString())))
				.andExpect(jsonPath("$.week[*].count", contains(1, 0, 1, 0, 1, 0, 0)));
	}

	@Test
	void theQueueOrderSurvivesTheMistakeAndHighlightMix() throws Exception {
		quiz("D1", "First?", null, false);
		clock.advance(Duration.ofMinutes(1));
		highlightCard("Second?");
		mvc.perform(get("/api/review/queue"))
				.andExpect(jsonPath("$[0].front", containsString("First?")))
				.andExpect(jsonPath("$[1].front", is("Second?")));
	}

	// ── The viewer's time zone ────────────────────────────────────────────────────────────────

	@Test
	void statsCountDaysInTheTimeZoneTheBrowserSends() throws Exception {
		String a = highlightCard("A?");
		grade(a, "AGAIN"); // 25 Nov 12:00 UTC: the 25th in both zones
		String b = highlightCard("B?");
		ReviewCard due = cards.findById(Long.parseLong(b)).orElseThrow();
		due.setDue(Instant.parse("2026-11-26T10:00:00Z")); // the 26th in both zones
		cards.save(due);
		clock.now = Instant.parse("2026-11-25T19:00:00Z"); // still the 25th in UTC, already 00:30 on the 26th in India

		mvc.perform(get("/api/review/stats").param("zone", "UTC"))
				.andExpect(jsonPath("$.reviewedToday", is(true)))
				.andExpect(jsonPath("$.streak", is(1)))
				.andExpect(jsonPath("$.week[0].date", is("2026-11-25")))
				.andExpect(jsonPath("$.week[*].count", contains(1, 1, 0, 0, 0, 0, 0)));
		mvc.perform(get("/api/review/stats").param("zone", "Asia/Kolkata"))
				.andExpect(jsonPath("$.reviewedToday", is(false)))
				.andExpect(jsonPath("$.streak", is(1)))
				.andExpect(jsonPath("$.week[0].date", is("2026-11-26")))
				.andExpect(jsonPath("$.week[*].count", contains(2, 0, 0, 0, 0, 0, 0)));
	}

	@Test
	void anUnknownTimeZoneIsRejected() throws Exception {
		mvc.perform(get("/api/review/stats").param("zone", "Mars/Olympus")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/review/stats")).andExpect(status().isOk());
	}

	// ── Mistakes from before flashcards ───────────────────────────────────────────────────────

	@Test
	void openMistakesWithoutACardGetOneOnceWhenTheQueueIsRead() throws Exception {
		QuizMistake open = mistake("Which loop is amortised O(n)?", false);
		mistake("Which map keeps insertion order?", true);

		mvc.perform(get("/api/review/queue"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].sourceKind", is("MISTAKE")))
				.andExpect(jsonPath("$[0].sourceId", is(open.getId().intValue())))
				.andExpect(jsonPath("$[0].front", is("Which loop is amortised O(n)?")));
		mvc.perform(get("/api/review/queue")).andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(get("/api/review/stats")).andExpect(jsonPath("$.dueNow", is(1)));
		assertThat(cards.count()).isEqualTo(1);
	}

	/** A mistake saved before cards existed: straight into the table, no card. */
	private QuizMistake mistake(String question, boolean fixed) {
		QuizMistake q = new QuizMistake();
		q.setModuleId("D1");
		q.setQuizKey(MistakeService.key(question));
		q.setQuestion(question);
		q.setCorrect("The two-pointer loop");
		q.setWrongCount(1);
		q.setFixed(fixed);
		q.setFirstWrongAt(NOON);
		q.setLastWrongAt(NOON);
		return mistakes.save(q);
	}
}
