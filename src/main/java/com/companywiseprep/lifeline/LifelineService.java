package com.companywiseprep.lifeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthropic.errors.AnthropicException;
import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.claude.ClaudeGateway;
import com.companywiseprep.gemini.GeminiGateway;
import com.companywiseprep.gemini.GeminiGateway.GeminiException;
import com.companywiseprep.settings.SettingsService;
import com.companywiseprep.settings.SettingsService.Reviewer;
import com.companywiseprep.web.NotFoundException;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Help while studying, without leaving the page: a nudge, the key idea, a full explanation, or an
 * answer to your own doubt, grounded in the question's model answer or the lesson you are on. From any
 * other page ("general") it answers doubts only, from the model's own knowledge.
 * Uses the same AI as the answer judge (Settings); without a key it falls back to hints taken
 * from the model answer itself.
 */
@Service
public class LifelineService {

	public enum Kind {
		NUDGE, IDEA, EXPLAIN, ASK
	}

	public record Turn(String q, String a) {
	}

	public record Help(String mode, String text, String note) {
	}

	record Reply(@JsonPropertyDescription("Your reply, in Markdown") String text) {
	}

	private static final Map<String, Object> GEMINI_SCHEMA = Map.of("type", "OBJECT",
			"properties", Map.of("text", Map.of("type", "STRING")), "required", List.of("text"));

	private static final String SYSTEM = """
			You are a patient senior Java backend engineer tutoring a candidate (5.5 years of Java and Spring Boot, \
			preparing for SDE II / Senior interviews in India) while they study. You help with the page they are on; \
			its reference material is given. Use it as the source of truth for facts.

			Keep them learning, not copying: give exactly the level of help asked for, no more.
			- NUDGE: one or two sentences that point their thinking in the right direction (a guiding question \
			or the area to think about). Do not state the answer or list the key points.
			- IDEA: the core idea in at most 4 short bullets, with the key terms in bold. No full answer.
			- EXPLAIN: explain it simply as if they are stuck: a plain-words picture or analogy first, then the \
			mechanism step by step, one short code or real-world example, then one line on how to say it in an \
			interview. At most 250 words.
			- ASK: answer their doubt directly and briefly (at most 150 words), then stop. If it is off-topic \
			for backend interviews, say so in one line.

			When the reference says GENERAL, there is no reference material: they are on another page of their \
			study app (named in <page>) and may ask about anything in backend engineering, Java, Spring, SQL, \
			system design, DSA, interviews or their study plan. Answer from your own knowledge in at most 250 \
			words; if you are not sure of a fact (a version, a default), say so instead of guessing.

			Plain Indian English, short sentences, Markdown. No preamble like "Great question".""";

	private static final int MAX_REFERENCE = 12_000;

	private final QuestionRepository questions;
	private final AcademyModuleRepository modules;
	private final ClaudeGateway claude;
	private final GeminiGateway gemini;
	private final SettingsService settings;

	public LifelineService(QuestionRepository questions, AcademyModuleRepository modules, ClaudeGateway claude,
			GeminiGateway gemini, SettingsService settings) {
		this.questions = questions;
		this.modules = modules;
		this.claude = claude;
		this.gemini = gemini;
		this.settings = settings;
	}

	@Transactional(readOnly = true)
	public Help help(String context, String ref, Kind kind, String doubt, List<Turn> history) {
		String title;
		String reference;
		boolean general = "general".equals(context);
		if (general) {
			// No page content to ground in, so no hint ladder: every request is a doubt.
			title = ref;
			reference = null;
			kind = Kind.ASK;
		} else if ("lesson".equals(context)) {
			AcademyModule m = modules.findById(ref).orElseThrow(() -> new NotFoundException("No lesson " + ref));
			title = m.getTitle();
			reference = m.getLesson();
		} else {
			Question q = questions.findById(ref).orElseThrow(() -> new NotFoundException("No question " + ref));
			title = q.getTitle() + (q.getPrompt() == null ? "" : "\n" + q.getPrompt().trim());
			reference = q.getAnswer();
		}
		String prompt = prompt(title, general ? "GENERAL" : reference, kind, doubt, history);

		List<String> notes = new ArrayList<>();
		Reviewer use = settings.effectiveReviewer();
		String text = null;
		String mode = null;
		if (use == Reviewer.CLAUDE || use == Reviewer.BOTH) {
			text = byClaude(prompt, notes);
			mode = "claude";
		}
		if (text == null && (use == Reviewer.GEMINI || use == Reviewer.BOTH)) {
			text = byGemini(prompt, notes);
			mode = "gemini";
		}
		if (text == null) {
			text = offline(kind, reference);
			mode = "offline";
			if (notes.isEmpty() && !general) {
				notes.add("Hints from the model answer. Add a Gemini or Claude key in Settings for a tutor that answers your own doubts.");
			}
		}
		return new Help(mode, text, notes.isEmpty() ? null : String.join(" ", notes));
	}

	private String byClaude(String prompt, List<String> notes) {
		try {
			String t = claude.structured(SYSTEM, prompt, Reply.class, 2_000).map(Reply::text).orElse(null);
			if (t == null) notes.add("Claude declined.");
			return t;
		} catch (AnthropicException e) {
			notes.add("Claude was unavailable (" + e.getMessage() + ").");
			return null;
		}
	}

	private String byGemini(String prompt, List<String> notes) {
		try {
			String t = gemini.structured(SYSTEM, prompt, GEMINI_SCHEMA, Reply.class).map(Reply::text).orElse(null);
			if (t == null) notes.add("Gemini declined.");
			return t;
		} catch (GeminiException e) {
			notes.add(e.getMessage() + ".");
			return null;
		}
	}

	private static String prompt(String title, String reference, Kind kind, String doubt, List<Turn> history) {
		String ref = reference == null ? "(none)"
				: reference.replaceAll("(?s)```(anim|mermaid)\\n.*?```", "").replaceAll("\\n{3,}", "\n\n").trim();
		if (ref.length() > MAX_REFERENCE) ref = ref.substring(0, MAX_REFERENCE) + "\n…";
		StringBuilder past = new StringBuilder();
		for (Turn t : history == null ? List.<Turn>of() : history.subList(Math.max(0, history.size() - 6), history.size())) {
			past.append("Candidate: ").append(t.q()).append("\nTutor: ").append(t.a()).append("\n\n");
		}
		return """
				<page>
				%s
				</page>
				<reference>
				%s
				</reference>
				<earlier_in_this_session>
				%s
				</earlier_in_this_session>
				<help_level>%s</help_level>
				<candidate_doubt>%s</candidate_doubt>""".formatted(title, ref, past.toString().trim(), kind,
				doubt == null ? "" : doubt.trim());
	}

	/** Without a key: the nudge is the answer's section headings, the idea its short answer. */
	static String offline(Kind kind, String reference) {
		String md = reference == null ? "" : reference;
		return switch (kind) {
			case NUDGE -> {
				List<String> heads = new ArrayList<>();
				Matcher m = Pattern.compile("(?m)^## (.+)$").matcher(md);
				while (m.find()) {
					String h = m.group(1).trim();
					if (!h.matches("(?i).*(watch it|picture it|short answer).*")) heads.add(h);
				}
				yield heads.isEmpty() ? "Start with a one-line definition, then explain how it works inside."
						: "Think through these, in order:\n\n" + String.join("\n", heads.stream().limit(6).map(h -> "- " + h).toList());
			}
			case IDEA -> {
				Matcher m = Pattern.compile("\\*\\*Short answer:\\*\\*\\s*([\\s\\S]*?)(?:\\n\\s*\\n|$)").matcher(md);
				yield m.find() ? m.group(1).trim() : "Open the model answer below and read its first section.";
			}
			case EXPLAIN -> "The full explanation is the model answer on this page. Read it once, then close it and say it out loud.";
			case ASK -> "Answering your own doubts needs a Gemini or Claude key (Settings).";
		};
	}
}
