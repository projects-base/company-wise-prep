package com.companywiseprep.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.companywiseprep.settings.SettingsService.Provider;
import com.companywiseprep.settings.SettingsService.Update;

class SettingsServiceTest {

	private final Map<String, String> rows = new HashMap<>();

	private SettingsService service(String secret) {
		AppSettingRepository repo = mock(AppSettingRepository.class);
		when(repo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(rows.get(i.<String>getArgument(0)))
				.map(v -> new AppSetting(i.getArgument(0), v)));
		when(repo.save(any())).thenAnswer(i -> {
			AppSetting s = i.getArgument(0);
			rows.put(s.getName(), s.getValue());
			return s;
		});
		doAnswer(i -> rows.remove(i.<String>getArgument(0))).when(repo).deleteById(anyString());
		return new SettingsService(repo, "claude-opus-5-5", "gemini-3.8-flash", "gemini-a, gemini-b", secret);
	}

	@Test
	void keysAreEncryptedAtRestAndNeverShownBack() {
		SettingsService s = service("a-long-random-app-token-secret");
		s.update(new Update("gemini", null, null, "AIza-test-key-abcd1234", null, null));

		assertThat(rows.get("gemini.key")).startsWith("enc:v1:").doesNotContain("AIza");
		assertThat(s.apiKey(Provider.GEMINI)).contains("AIza-test-key-abcd1234");
		var view = s.view();
		assertThat(view.gemini().keyHint()).isEqualTo("…1234");
		assertThat(view.toString()).doesNotContain("AIza-test-key");
		assertThat(view.effective()).isEqualTo("gemini");
	}

	@Test
	void anEmptyKeyRemovesItAndTheReviewerFallsBack() {
		SettingsService s = service("");
		s.update(new Update("both", "sk-ant-test-0000", null, null, "gemini-custom", null));
		assertThat(rows.get("anthropic.key")).startsWith("plain:");
		assertThat(s.effectiveReviewer()).isEqualTo(SettingsService.Reviewer.CLAUDE);
		assertThat(s.model(Provider.GEMINI)).isEqualTo("gemini-custom");

		s.update(new Update(null, "", null, null, "", null));
		assertThat(s.apiKey(Provider.CLAUDE)).isEmpty();
		assertThat(s.model(Provider.GEMINI)).isEqualTo("gemini-3.8-flash");
		assertThat(s.effectiveReviewer()).isEqualTo(SettingsService.Reviewer.OFFLINE);
	}

	@Test
	void fallbacksAreAnOrderedListThatResetsWhenCleared() {
		SettingsService s = service("");
		assertThat(s.fallbacks(Provider.GEMINI)).containsExactly("gemini-a", "gemini-b");
		assertThat(s.fallbacks(Provider.CLAUDE)).isEmpty();

		s.update(new Update(null, null, null, null, null, " gemini-x,\ngemini-y , gemini-x "));
		assertThat(s.fallbacks(Provider.GEMINI)).containsExactly("gemini-x", "gemini-y");

		s.update(new Update(null, null, null, null, null, ""));
		assertThat(s.fallbacks(Provider.GEMINI)).containsExactly("gemini-a", "gemini-b");
	}
}
