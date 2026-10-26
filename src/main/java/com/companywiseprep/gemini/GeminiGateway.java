package com.companywiseprep.gemini;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.companywiseprep.settings.SettingsService;
import com.companywiseprep.settings.SettingsService.Provider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Google Gemini as a second reviewer, over its REST API (generateContent with a JSON response
 * schema). The key goes in the x-goog-api-key header, never in the URL, so it can't leak into
 * logs. Key and model come from the Settings page or GEMINI_API_KEY.
 */
@Component
public class GeminiGateway {

	private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

	private final SettingsService settings;
	private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public GeminiGateway(SettingsService settings) {
		this.settings = settings;
	}

	/** A reply Gemini could not give (bad key, quota, blocked): the message says why. */
	public static class GeminiException extends RuntimeException {
		public GeminiException(String message) {
			super(message);
		}
	}

	public boolean enabled() {
		return settings.apiKey(Provider.GEMINI).isPresent();
	}

	public String model() {
		return settings.model(Provider.GEMINI);
	}

	/**
	 * One request whose reply is JSON matching {@code schema} (OpenAPI-style, as Gemini expects),
	 * parsed into {@code type}. Empty when no key is set or the reply was blocked.
	 */
	public <T> Optional<T> structured(String system, String user, Map<String, Object> schema, Class<T> type) {
		Optional<String> key = settings.apiKey(Provider.GEMINI);
		if (key.isEmpty()) return Optional.empty();
		Map<String, Object> body = Map.of(
				"systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
				"contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", user)))),
				"generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", schema));
		Optional<String> text = call(key.get(), body);
		if (text.isEmpty()) return Optional.empty();
		try {
			return Optional.of(json.readValue(text.get(), type));
		} catch (IOException e) {
			throw new GeminiException("Gemini's reply was not valid JSON");
		}
	}

	/**
	 * Speech to text for the answer box, for browsers whose own speech service is unavailable
	 * (blocked network, Brave, Firefox). {@code audio} is a short WAV recording.
	 */
	public String transcribe(byte[] audio, String mimeType) {
		String key = settings.apiKey(Provider.GEMINI)
				.orElseThrow(() -> new GeminiException("Transcription needs a Gemini API key (Settings)."));
		Map<String, Object> body = Map.of("contents", List.of(Map.of("role", "user", "parts", List.of(
				Map.of("inlineData", Map.of("mimeType", mimeType, "data", Base64.getEncoder().encodeToString(audio))),
				Map.of("text", """
						Transcribe this recording word for word, in English. It is a candidate answering a Java \
						backend interview question, often with an Indian accent. Spell technical terms the way \
						they are written in code (ApplicationContext, BeanPostProcessor, @Transactional, \
						CompletableFuture, HashMap). Leave out filler sounds (um, uh). Reply with only the \
						transcript, or with nothing if there is no speech.""")))));
		return call(key, body).map(String::trim).orElse("");
	}

	/** A tiny request to check the key and model work (Settings → Test). */
	public String ping() {
		String key = settings.apiKey(Provider.GEMINI).orElseThrow(() -> new IllegalStateException("No Gemini API key is set."));
		return call(key, Map.of("contents", List.of(Map.of("role", "user",
				"parts", List.of(Map.of("text", "Reply with just the word OK."))))))
				.map(String::trim).orElse("(blocked)");
	}

	/**
	 * Overload (503) and rate limits (429) are usually brief: retry twice with a back-off, then try
	 * the lighter model once, so a busy moment at Google doesn't fail the review.
	 */
	private Optional<String> call(String key, Map<String, Object> body) {
		String model = model();
		GeminiException last = null;
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				return call(key, model, body);
			} catch (Busy e) {
				last = e;
				pause(attempt == 0 ? 2_000 : 5_000);
			}
		}
		if (!model.equals(FALLBACK_MODEL)) {
			try {
				return call(key, FALLBACK_MODEL, body);
			} catch (Busy e) {
				throw new GeminiException(last.getMessage() + " (" + FALLBACK_MODEL + " was busy too; try again in a minute)");
			}
		}
		throw last;
	}

	private static final String FALLBACK_MODEL = "gemini-3.5-flash-lite";

	/** HTTP 503 or 429: worth retrying. */
	private static class Busy extends GeminiException {
		Busy(String message) {
			super(message);
		}
	}

	private static void pause(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new GeminiException("Interrupted while waiting for Gemini");
		}
	}

	private Optional<String> call(String key, String model, Map<String, Object> body) {
		try {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE + URLEncoder.encode(model, StandardCharsets.UTF_8) + ":generateContent"))
					.timeout(Duration.ofSeconds(90))
					.header("Content-Type", "application/json")
					.header("x-goog-api-key", key)
					.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
					.build();
			HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
			JsonNode root = json.readTree(res.body());
			if (res.statusCode() / 100 != 2) {
				String msg = root.path("error").path("message").asText("");
				String text = "Gemini (" + model + ") returned HTTP " + res.statusCode() + (msg.isBlank() ? "" : ": " + msg);
				if (res.statusCode() == 503 || res.statusCode() == 429) throw new Busy(text);
				throw new GeminiException(text);
			}
			JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
			StringBuilder out = new StringBuilder();
			for (JsonNode p : parts) {
				if (!p.path("thought").asBoolean(false)) out.append(p.path("text").asText(""));
			}
			return out.isEmpty() ? Optional.empty() : Optional.of(out.toString());
		} catch (IOException e) {
			throw new GeminiException("Could not reach Gemini: " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new GeminiException("Interrupted while waiting for Gemini");
		}
	}
}
