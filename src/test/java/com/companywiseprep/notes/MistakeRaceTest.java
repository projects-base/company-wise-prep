package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.companywiseprep.notes.MistakeService.Attempt;
import com.companywiseprep.notes.MistakeService.MistakeView;

/**
 * Two first wrong answers for the same quiz arriving together: the second insert hits the unique constraint. Simulated
 * by making the lookup miss a row that is already there; the service must re-read and apply the update, not fail.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:mistakerace;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
class MistakeRaceTest {

	@Autowired
	MistakeService service;

	@MockitoSpyBean
	QuizMistakeRepository mistakes;

	@Test
	void aConcurrentFirstWrongAnswerIsMergedIntoTheExistingMistake() {
		Attempt wrong = new Attempt("J1", "Which tool runs bytecode?", "javac", "java", "The launcher starts the JVM.", null,
				false);
		service.record(wrong);

		// The next lookup misses the row (as if the other request had not committed yet); later lookups see it.
		AtomicInteger calls = new AtomicInteger();
		doAnswer(inv -> calls.getAndIncrement() == 0 ? Optional.empty()
				: mistakes.findAll().stream()
						.filter(q -> q.getModuleId().equals(inv.getArgument(0)) && q.getQuizKey().equals(inv.getArgument(1)))
						.findFirst())
				.when(mistakes).findByModuleIdAndQuizKey(anyString(), anyString());
		Optional<MistakeView> second = service.record(wrong);

		assertThat(second).map(MistakeView::wrongCount).contains(2);
		assertThat(mistakes.count()).isEqualTo(1);
	}
}
