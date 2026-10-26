package com.companywiseprep.claude;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.companywiseprep.settings.SettingsService;
import com.companywiseprep.settings.SettingsService.Provider;

/**
 * The only way the app talks to Claude (CLAUDE.md). The key and model come from the Settings page,
 * or ANTHROPIC_API_KEY in the environment; without a key every call returns empty and callers fall
 * back to their offline path.
 */
@Component
public class ClaudeGateway {

	private final SettingsService settings;
	private AnthropicClient client;
	private String clientKey;

	public ClaudeGateway(SettingsService settings) {
		this.settings = settings;
	}

	public boolean enabled() {
		return settings.apiKey(Provider.CLAUDE).isPresent();
	}

	public String model() {
		return settings.model(Provider.CLAUDE);
	}

	/**
	 * One request whose reply is constrained to {@code type}'s JSON schema. Empty when Claude is
	 * not configured or declined (a refusal); API errors propagate so the caller can say why.
	 */
	public <T> Optional<T> structured(String system, String user, Class<T> type, long maxTokens) {
		Optional<AnthropicClient> c = client();
		if (c.isEmpty()) return Optional.empty();
		StructuredMessageCreateParams<T> params = MessageCreateParams.builder()
				.model(model())
				.maxTokens(maxTokens)
				.system(system)
				.outputConfig(type)
				.addUserMessage(user)
				.build();
		StructuredMessage<T> reply = c.get().messages().create(params);
		if (reply.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) return Optional.empty();
		return reply.content().stream().flatMap(block -> block.text().stream()).map(text -> text.text()).findFirst();
	}

	/** A tiny request to check the key and model work (Settings → Test). */
	public String ping() {
		AnthropicClient c = client().orElseThrow(() -> new IllegalStateException("No Claude API key is set."));
		var reply = c.messages().create(MessageCreateParams.builder()
				.model(model())
				.maxTokens(2_000L)
				.addUserMessage("Reply with just the word OK.")
				.build());
		return reply.content().stream().flatMap(b -> b.text().stream()).map(t -> t.text().trim()).findFirst().orElse("");
	}

	/** One client per key, rebuilt when the key changes in Settings. */
	private synchronized Optional<AnthropicClient> client() {
		Optional<String> key = settings.apiKey(Provider.CLAUDE);
		if (key.isEmpty()) return Optional.empty();
		if (client == null || !key.get().equals(clientKey)) {
			client = AnthropicOkHttpClient.builder().apiKey(key.get()).build();
			clientKey = key.get();
		}
		return Optional.of(client);
	}
}
