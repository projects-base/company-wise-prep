package com.companywiseprep.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.companywiseprep.code.JavaRunnerAccess;

/** The app as deployed: password login, CORS for the Netlify UI, no localhost-only guard. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:hostedtest;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data",
		"prep.auth.enabled=true",
		"prep.auth.password=correct horse battery",
		"prep.auth.token-secret=0123456789abcdef0123456789abcdef-test",
		"prep.cors.allowed-origins=https://cwp.netlify.app"})
@AutoConfigureMockMvc
class HostedModeTest {

	static final String UI = "https://cwp.netlify.app";

	@Autowired
	MockMvc mvc;

	@Autowired
	AuthService auth;

	private String login(String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\":\"" + password + "\"}"))
				.andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(body, "$.token");
	}

	@Test
	void theApiNeedsALoginAndAValidTokenOpensIt() throws Exception {
		mvc.perform(get("/api/auth/status"))
				.andExpect(jsonPath("$.authRequired", is(true)))
				.andExpect(jsonPath("$.authenticated", is(false)));
		mvc.perform(get("/api/questions")).andExpect(status().isUnauthorized());

		String token = login("correct horse battery");
		// Hosted: the Host header is the Render domain, not localhost — must still work.
		mvc.perform(get("/api/questions").header("Authorization", "Bearer " + token).header("Host", "cwp.onrender.com"))
				.andExpect(status().isOk());
		mvc.perform(get("/api/auth/status").header("Authorization", "Bearer " + token))
				.andExpect(jsonPath("$.authenticated", is(true)));
	}

	@Test
	void forgedOrTamperedTokensAreRejected() throws Exception {
		String token = login("correct horse battery");
		String expiry = token.substring(0, token.indexOf('.'));
		String later = Long.toString(Long.parseLong(expiry) + 365L * 86400);
		mvc.perform(get("/api/questions").header("Authorization", "Bearer " + later + token.substring(token.indexOf('.'))))
				.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/questions").header("Authorization", "Bearer nonsense")).andExpect(status().isUnauthorized());
	}

	@Test
	void onlyTheNetlifySiteMayCallTheApiFromABrowser() throws Exception {
		mvc.perform(options("/api/questions").header("Origin", UI).header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", UI));
		mvc.perform(options("/api/questions").header("Origin", "https://evil.example")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
		// A 401 still carries CORS headers, so the UI can show its login screen.
		mvc.perform(get("/api/questions").header("Origin", UI))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("Access-Control-Allow-Origin", UI));
	}

	@Test
	void wrongPasswordsAreRefused() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"guess\"}"))
				.andExpect(status().isUnauthorized());
	}

	/** Own instance: locking the shared one would break the other tests in this context. */
	@Test
	void repeatedWrongPasswordsLockLoginForAWhile() {
		java.time.Instant t0 = java.time.Instant.parse("2026-10-08T10:00:00Z");
		java.time.Clock clock = java.time.Clock.fixed(t0, java.time.ZoneOffset.UTC);
		AuthService fresh = new AuthService(true, "correct horse battery", "0123456789abcdef0123456789abcdef-test", clock);
		for (int i = 0; i < AuthService.MAX_FAILURES; i++) {
			assertThat(fresh.login("guess" + i).result()).isEqualTo(AuthService.LoginResult.WRONG);
		}
		assertThat(fresh.login("correct horse battery").result()).isEqualTo(AuthService.LoginResult.LOCKED);
		AuthService later = new AuthService(true, "correct horse battery", "0123456789abcdef0123456789abcdef-test",
				java.time.Clock.fixed(t0.plus(AuthService.FAILURE_WINDOW).plusSeconds(1), java.time.ZoneOffset.UTC));
		assertThat(later.login("correct horse battery").result()).isEqualTo(AuthService.LoginResult.OK);
	}

	@Test
	void refusesToStartWithAWeakPassword() {
		org.assertj.core.api.Assertions.assertThatThrownBy(
				() -> new AuthService(true, "short", "0123456789abcdef0123456789abcdef-test", java.time.Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void submittedCodeCannotSeeServerSecrets() {
		Map<String, String> env = new HashMap<>(Map.of("PATH", "/usr/bin", "DB_PASSWORD", "x", "APP_PASSWORD", "y",
				"APP_TOKEN_SECRET", "z", "HOME", "/home/app"));
		JavaRunnerAccess.restrict(env);
		assertThat(env).containsOnlyKeys("PATH", "HOME");
	}
}
