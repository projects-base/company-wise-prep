package com.companywiseprep.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class OfflineReviewerTest {

	private static final String MODEL = """
			**Short answer:** The **ApplicationContext** is the IoC container. Beans go through \
			**instantiate**, **inject**, then `@PostConstruct`.

			## Explanation

			A `BeanPostProcessor` creates the **AOP proxy** after init.

			## Pitfalls and follow-ups

			- **Self-invocation** skips the proxy.
			- **Prototype beans** get no destroy callback.
			""";

	@Test
	void groupsTermsBySectionAndSkipsTheLabel() {
		var terms = OfflineReviewer.termsByLayer(MODEL);
		assertThat(terms.get("what")).containsExactly("ApplicationContext", "instantiate", "inject", "@PostConstruct");
		assertThat(terms.get("how")).containsExactly("BeanPostProcessor", "AOP proxy");
		assertThat(terms.get("pitfalls")).containsExactly("Self-invocation", "Prototype beans");
	}

	@Test
	void spokenWordsMatchCodeTerms() {
		String said = OfflineReviewer.norm("the application context calls post construct after it can inject things");
		assertThat(OfflineReviewer.mentions(said, "ApplicationContext")).isTrue();
		assertThat(OfflineReviewer.mentions(said, "@PostConstruct")).isTrue();
		assertThat(OfflineReviewer.mentions(said, "BeanPostProcessor")).isFalse();
	}

	@Test
	void aFullerAnswerWithAnExampleScoresHigher() {
		String thin = "It is the container that holds the beans and creates them for the application.";
		String full = "The application context is the IoC container. It will instantiate the bean, inject dependencies, "
				+ "then run post construct. After init a bean post processor wraps it in an AOP proxy. In my project I used "
				+ "post construct to warm a cache. Watch out for self invocation, which skips the proxy, and prototype beans "
				+ "never get the destroy callback.";
		AnswerReview a = OfflineReviewer.review(MODEL, List.of("Bean scopes?"), thin);
		AnswerReview b = OfflineReviewer.review(MODEL, List.of("Bean scopes?"), full);
		assertThat(a.mode()).isEqualTo("offline");
		assertThat(b.score()).isGreaterThan(a.score()).isGreaterThanOrEqualTo(7.5);
		assertThat(b.layers()).extracting(AnswerReview.Layer::key).containsExactly("what", "how", "where", "pitfalls");
		assertThat(b.missed()).isEmpty();
		assertThat(b.followUp()).isEqualTo("Bean scopes?");
		assertThat(b.improvedAnswer()).startsWith("The **ApplicationContext** is the IoC container.");
	}
}
