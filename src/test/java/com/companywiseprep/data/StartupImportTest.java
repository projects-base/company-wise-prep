package com.companywiseprep.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import com.companywiseprep.bank.QuestionRepository;

/**
 * Cloud Run throttles the CPU to almost nothing once the port is open and no request is running, so
 * an import that starts after the web server can take many minutes and be killed by Neon's
 * idle-in-transaction timeout. The import must finish before the server opens its port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.datasource.url=jdbc:h2:mem:startupimport;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
class StartupImportTest {

	@TestConfiguration
	static class Config {
		static final AtomicLong questionsWhenPortOpened = new AtomicLong(-1);

		@Bean
		ApplicationListener<WebServerInitializedEvent> recordQuestions(QuestionRepository questions) {
			return e -> questionsWhenPortOpened.set(questions.count());
		}
	}

	@Autowired
	QuestionRepository questions;

	@Test
	void dataIsImportedBeforeTheServerAcceptsRequests() {
		assertThat(Config.questionsWhenPortOpened.get()).isPositive().isEqualTo(questions.count());
	}
}
