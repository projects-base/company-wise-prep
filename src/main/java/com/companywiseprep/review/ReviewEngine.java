package com.companywiseprep.review;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.anthropic.errors.AnthropicException;
import com.companywiseprep.claude.ClaudeGateway;
import com.companywiseprep.gemini.GeminiGateway;
import com.companywiseprep.gemini.GeminiGateway.GeminiException;
import com.companywiseprep.settings.SettingsService;
import com.companywiseprep.settings.SettingsService.Reviewer;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Judges one spoken or typed explanation against a reference: with Claude, Gemini or both (Settings), otherwise with
 * the offline term check. Stores nothing; the callers keep their own attempts (a question's answers, a lesson's
 * explanations).
 */
@Service
public class ReviewEngine {

	static final String OFFLINE_NOTE = "Offline check: it compares key terms only. Add a Claude or Gemini API key in Settings for a full review.";

	private static final String SYSTEM = """
			You are a senior backend engineer interviewing a candidate for an SDE II / Senior Java backend role \
			in India. You judge one answer to a theory question.

			The answer was usually spoken and transcribed, so ignore filler words, missing punctuation and obvious \
			speech-to-text slips (for example "been" for "bean"); judge the substance.

			Score four layers from 0 to 10:
			- what: a correct, crisp definition given up front.
			- how: how it works inside: the mechanism, the order of steps, and why.
			- where: a concrete example of real use. A generic textbook example scores at most 5; none scores 0 to 2.
			- pitfalls: what goes wrong, trade-offs, edge cases, the follow-ups an interviewer would probe.

			The overall score (0 to 10) is how an experienced interviewer would rate the answer for SDE II: \
			9-10 strong hire, 7-8 good, 5-6 borderline (mostly definitions), below 5 weak or wrong. \
			A factual error costs more than an omission.

			Use the reference answer as the source of truth for facts, but do not require its wording, and credit \
			correct points it does not mention.

			Write for an Indian English speaker in plain words. Be direct and encouraging.""";

	/** The shape both reviewers must reply in (structured output). */
	record ClaudeReview(
			@JsonPropertyDescription("Overall score from 0 to 10, one decimal") double score,
			@JsonPropertyDescription("One sentence of feedback") String verdict,
			@JsonPropertyDescription("Exactly four layers, in order: what, how, where, pitfalls") List<ClaudeLayer> layers,
			@JsonPropertyDescription("Points the candidate got right, as short phrases") List<String> covered,
			@JsonPropertyDescription("The most important missing points, most important first, at most 6") List<String> missed,
			@JsonPropertyDescription("Wrong or misleading statements, each followed by the correction; empty if none") List<String> mistakes,
			@JsonPropertyDescription("How the candidate should have said it: spoken style, 90 to 140 words, in the order what, how, where, pitfalls, keeping their own example if they gave one") String improvedAnswer,
			@JsonPropertyDescription("The single next question you would ask to probe the weakest layer") String followUp) {
	}

	record ClaudeLayer(
			@JsonPropertyDescription("One of: what, how, where, pitfalls") String key,
			@JsonPropertyDescription("Score from 0 to 10") double score,
			@JsonPropertyDescription("One short sentence on this layer") String comment) {
	}

	/** The same shape for Gemini, whose schema dialect is OpenAPI-style. */
	private static final Map<String, Object> GEMINI_SCHEMA = geminiSchema();

	/**
	 * What is being judged.
	 *
	 * @param title the question as asked
	 * @param prompt more of the question, or null
	 * @param reference the model answer or lesson text (Markdown); null when there is none
	 */
	public record Brief(String title, String prompt, List<String> followUps, String reference, String answer) {
	}

	private final ClaudeGateway claude;
	private final GeminiGateway gemini;
	private final SettingsService settings;

	public ReviewEngine(ClaudeGateway claude, GeminiGateway gemini, SettingsService settings) {
		this.claude = claude;
		this.gemini = gemini;
		this.settings = settings;
	}

	/** The review, with Gemini's as {@code second} when both reviewers are on, and a note on anything that fell back. */
	public AnswerReview judge(Brief brief) {
		String prompt = prompt(brief);
		Reviewer use = settings.effectiveReviewer();
		List<String> notes = new ArrayList<>();

		AnswerReview primary = null;
		AnswerReview second = null;
		if (use == Reviewer.CLAUDE || use == Reviewer.BOTH) primary = byClaude(prompt, notes);
		if (use == Reviewer.GEMINI || use == Reviewer.BOTH) {
			AnswerReview g = byGemini(prompt, notes);
			if (primary == null) primary = g;
			else second = g;
		}
		if (primary == null) {
			notes.add(OFFLINE_NOTE);
			primary = OfflineReviewer.review(brief.reference(), brief.followUps(), brief.answer());
		}
		return primary.withSecond(second).withNote(notes.isEmpty() ? null : String.join(" ", notes));
	}

	/** The score a trend records: with two reviewers, their average. */
	public static double trendScore(AnswerReview r) {
		return r.second() == null ? r.score() : Math.round((r.score() + r.second().score()) * 5) / 10.0;
	}

	/** The reviewer(s) a trend records: "claude", "gemini", "offline" or "claude+gemini". */
	public static String trendMode(AnswerReview r) {
		return r.second() == null ? r.mode() : r.mode() + "+" + r.second().mode();
	}

	private AnswerReview byClaude(String prompt, List<String> notes) {
		try {
			AnswerReview r = claude.structured(SYSTEM, prompt, ClaudeReview.class, 8_000)
					.map(x -> fromReply("claude", x)).orElse(null);
			if (r == null) notes.add("Claude declined to review this answer.");
			return r;
		} catch (AnthropicException e) {
			notes.add("Claude was unavailable (" + e.getMessage() + ").");
			return null;
		}
	}

	private AnswerReview byGemini(String prompt, List<String> notes) {
		try {
			AnswerReview r = gemini.structured(SYSTEM, prompt, GEMINI_SCHEMA, ClaudeReview.class)
					.map(x -> fromReply("gemini", x)).orElse(null);
			if (r == null) notes.add("Gemini declined to review this answer.");
			return r;
		} catch (GeminiException e) {
			notes.add(e.getMessage() + ".");
			return null;
		}
	}

	private static String prompt(Brief b) {
		// Animations and diagrams are for the eyes; leave them out of the reference.
		String reference = b.reference() == null ? "(none)"
				: b.reference().replaceAll("(?s)```(anim|mermaid)\\n.*?```", "").replaceAll("\\n{3,}", "\n\n");
		return """
				<question>
				%s
				%s
				</question>
				<interviewer_follow_ups>
				%s
				</interviewer_follow_ups>
				<reference_answer>
				%s
				</reference_answer>
				<candidate_answer>
				%s
				</candidate_answer>""".formatted(b.title(), b.prompt() == null ? "" : b.prompt().trim(),
				String.join("\n", b.followUps()), reference.trim(), b.answer().trim());
	}

	private static AnswerReview fromReply(String mode, ClaudeReview r) {
		List<AnswerReview.Layer> layers = new ArrayList<>();
		for (String key : List.of("what", "how", "where", "pitfalls")) {
			ClaudeLayer l = r.layers() == null ? null
					: r.layers().stream().filter(x -> key.equalsIgnoreCase(x.key())).findFirst().orElse(null);
			layers.add(new AnswerReview.Layer(key, OfflineReviewer.name(key), l == null ? null : clamp(l.score()),
					l == null ? "" : l.comment()));
		}
		return new AnswerReview(mode, clamp(r.score()), r.verdict(), layers, nz(r.covered()), nz(r.missed()),
				nz(r.mistakes()), r.improvedAnswer(), r.followUp(), null, List.of(), null);
	}

	private static double clamp(double v) {
		return Math.round(Math.max(0, Math.min(10, v)) * 10) / 10.0;
	}

	private static List<String> nz(List<String> l) {
		return l == null ? List.of() : l;
	}

	private static Map<String, Object> geminiSchema() {
		Map<String, Object> str = Map.of("type", "STRING");
		Map<String, Object> num = Map.of("type", "NUMBER");
		Map<String, Object> strs = Map.of("type", "ARRAY", "items", str);
		Map<String, Object> layerProps = new LinkedHashMap<>();
		layerProps.put("key", Map.of("type", "STRING", "enum", List.of("what", "how", "where", "pitfalls")));
		layerProps.put("score", num);
		layerProps.put("comment", str);
		Map<String, Object> layer = Map.of("type", "OBJECT", "properties", layerProps,
				"required", List.of("key", "score", "comment"));
		Map<String, Object> props = new LinkedHashMap<>();
		props.put("score", num);
		props.put("verdict", str);
		props.put("layers", Map.of("type", "ARRAY", "items", layer));
		props.put("covered", strs);
		props.put("missed", strs);
		props.put("mistakes", strs);
		props.put("improvedAnswer", str);
		props.put("followUp", str);
		return Map.of("type", "OBJECT", "properties", props, "required", List.copyOf(props.keySet()),
				"propertyOrdering", List.copyOf(props.keySet()));
	}
}
