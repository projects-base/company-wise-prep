package com.companywiseprep.notes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.web.NotFoundException;

/**
 * The mistakes notebook, filled automatically from ```quiz answers on lesson pages. A wrong answer opens (or re-opens)
 * the quiz's mistake; a right answer closes it as fixed; a right answer with nothing open stores nothing.
 */
@Service
public class MistakeService {

	private final QuizMistakeRepository mistakes;
	private final AcademyModuleRepository modules;
	private final Clock clock;
	private final TransactionTemplate tx;

	public MistakeService(QuizMistakeRepository mistakes, AcademyModuleRepository modules, Clock clock,
			PlatformTransactionManager transactions) {
		this.mistakes = mistakes;
		this.modules = modules;
		this.clock = clock;
		this.tx = new TransactionTemplate(transactions);
	}

	public record Attempt(String moduleId, String question, String chosen, String correct, String why, String level,
			boolean right) {
	}

	public record MistakeView(long id, String moduleId, String moduleCode, String moduleTitle, String trackId, String question,
			String chosen, String correct, String why, String level, int wrongCount, boolean fixed, Instant firstWrongAt,
			Instant lastWrongAt, Instant fixedAt) {

		static MistakeView of(QuizMistake q, AcademyModule m) {
			return new MistakeView(q.getId(), q.getModuleId(), m == null ? q.getModuleId() : m.getCode(),
					m == null ? null : m.getTitle(), m == null ? null : m.getTrackId(), q.getQuestion(), q.getChosen(),
					q.getCorrect(), q.getWhy(), q.getLevel(), q.getWrongCount(), q.isFixed(), q.getFirstWrongAt(),
					q.getLastWrongAt(), q.getFixedAt());
		}
	}

	/**
	 * Each attempt runs in its own transaction. Two first wrong answers for one quiz can race (a double click, two tabs):
	 * the loser's insert hits the unique constraint, so it is retried once, and the retry finds the winner's row.
	 *
	 * @return the mistake as it now stands, or empty when a right answer had nothing to fix
	 */
	public Optional<MistakeView> record(Attempt attempt) {
		Attempt a = clean(attempt);
		try {
			return tx.execute(status -> apply(a));
		} catch (DataIntegrityViolationException raced) {
			return tx.execute(status -> apply(a));
		}
	}

	private static Attempt clean(Attempt a) {
		String question = Text.trim(a.question());
		if (question == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question must not be blank");
		return new Attempt(a.moduleId(), question, Text.clean(a.chosen()), Text.clean(a.correct()), Text.clean(a.why()),
				a.level(), a.right());
	}

	private Optional<MistakeView> apply(Attempt a) {
		AcademyModule m = modules.findById(a.moduleId()).orElseThrow(() -> new NotFoundException("No module " + a.moduleId()));
		String key = key(a.question());
		Optional<QuizMistake> existing = mistakes.findByModuleIdAndQuizKey(m.getId(), key);
		Instant now = Instant.now(clock);
		if (a.right()) {
			return existing.map(q -> {
				if (!q.isFixed()) {
					q.setFixed(true);
					q.setFixedAt(now);
				}
				return MistakeView.of(mistakes.save(q), m);
			});
		}
		QuizMistake q = existing.orElseGet(() -> {
			QuizMistake fresh = new QuizMistake();
			fresh.setModuleId(m.getId());
			fresh.setQuizKey(key);
			fresh.setFirstWrongAt(now);
			return fresh;
		});
		q.setQuestion(a.question());
		q.setChosen(a.chosen());
		q.setCorrect(a.correct());
		q.setWhy(a.why());
		q.setLevel(a.level());
		q.setWrongCount(q.getWrongCount() + 1);
		q.setLastWrongAt(now);
		q.setFixed(false);
		q.setFixedAt(null);
		return Optional.of(MistakeView.of(mistakes.save(q), m));
	}

	/** Open mistakes first, newest first within each group. */
	@Transactional(readOnly = true)
	public List<MistakeView> list() {
		return views(mistakes.findAll());
	}

	List<MistakeView> views(List<QuizMistake> rows) {
		Map<String, AcademyModule> byId = modules.findAllById(rows.stream().map(QuizMistake::getModuleId).distinct().toList())
				.stream().collect(Collectors.toMap(AcademyModule::getId, Function.identity()));
		return rows.stream()
				.sorted(Comparator.comparing(QuizMistake::isFixed)
						.thenComparing(QuizMistake::getLastWrongAt, Comparator.reverseOrder()))
				.map(q -> MistakeView.of(q, byId.get(q.getModuleId())))
				.toList();
	}

	/** Quiz identity within a lesson: a stable hash of the question text, whitespace-normalised. */
	static String key(String question) {
		String normalised = question.strip().replaceAll("\\s+", " ");
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(normalised.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash).substring(0, 16);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is always available", e);
		}
	}
}
