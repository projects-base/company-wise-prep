package com.companywiseprep.settings;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Settings saved from the Settings page: the AI reviewer API keys, their models and which
 * reviewer to use. A key saved here wins over the environment variable (ANTHROPIC_API_KEY,
 * GEMINI_API_KEY). Keys are never sent back to the browser, only whether one is set and its last
 * four characters. When APP_TOKEN_SECRET is set (always on the hosted app) keys are encrypted with
 * AES-GCM; the local app stores them in its own git-ignored database file.
 */
@Service
public class SettingsService {

	public enum Provider {
		CLAUDE("anthropic", "ANTHROPIC_API_KEY"), GEMINI("gemini", "GEMINI_API_KEY");

		final String id;
		final String env;

		Provider(String id, String env) {
			this.id = id;
			this.env = env;
		}
	}

	/** auto: Claude if it has a key, else Gemini, else offline. both: Claude and Gemini side by side. */
	public enum Reviewer {
		AUTO, CLAUDE, GEMINI, BOTH, OFFLINE
	}

	/** {@code fallbacks}: models tried in order when the main one is busy or failing (Gemini only). */
	public record ProviderView(boolean keySet, String keyHint, String keySource, String model, String defaultModel,
			List<String> fallbacks, List<String> defaultFallbacks) {
	}

	public record View(String reviewer, String effective, ProviderView claude, ProviderView gemini, boolean encrypted) {
	}

	/** Null fields are left alone; an empty value removes the saved one (back to the default). */
	public record Update(String reviewer, String claudeKey, String claudeModel, String geminiKey, String geminiModel,
			String geminiFallbacks) {
	}

	private static final String ENC = "enc:v1:";
	private static final String PLAIN = "plain:";

	private final AppSettingRepository repo;
	private final String claudeDefault;
	private final String geminiDefault;
	private final List<String> geminiFallbacksDefault;
	private final SecretKeySpec cipherKey;
	private final SecureRandom random = new SecureRandom();

	public SettingsService(AppSettingRepository repo,
			@Value("${prep.claude.model:claude-opus-5-5}") String claudeDefault,
			@Value("${prep.gemini.model:gemini-3.6-flash}") String geminiDefault,
			@Value("${prep.gemini.fallbacks:gemini-3.5-flash-lite,gemini-3.1-flash-lite,gemini-3.8-flash}") String geminiFallbacks,
			@Value("${prep.auth.token-secret:}") String secret) {
		this.repo = repo;
		this.claudeDefault = claudeDefault;
		this.geminiDefault = geminiDefault;
		this.geminiFallbacksDefault = split(geminiFallbacks);
		this.cipherKey = secret == null || secret.isBlank() ? null : new SecretKeySpec(sha256(secret), "AES");
	}

	@Transactional(readOnly = true)
	public Optional<String> apiKey(Provider p) {
		Optional<String> saved = read(p.id + ".key");
		if (saved.isPresent()) return saved;
		String env = System.getenv(p.env);
		return env == null || env.isBlank() ? Optional.empty() : Optional.of(env.trim());
	}

	@Transactional(readOnly = true)
	public String model(Provider p) {
		return read(p.id + ".model").orElse(p == Provider.CLAUDE ? claudeDefault : geminiDefault);
	}

	/** Models to try, in order, after the main one. Claude has none: its SDK retries overloads itself. */
	@Transactional(readOnly = true)
	public List<String> fallbacks(Provider p) {
		if (p == Provider.CLAUDE) return List.of();
		return read(p.id + ".fallbacks").map(SettingsService::split).orElse(geminiFallbacksDefault);
	}

	private static List<String> split(String csv) {
		return Arrays.stream(csv.split("[,\\s]+")).map(String::trim).filter(m -> !m.isEmpty()).distinct().limit(6).toList();
	}

	@Transactional(readOnly = true)
	public Reviewer reviewer() {
		try {
			return read("review.mode").map(s -> Reviewer.valueOf(s.toUpperCase(Locale.ROOT))).orElse(Reviewer.AUTO);
		} catch (IllegalArgumentException e) {
			return Reviewer.AUTO;
		}
	}

	/** What a review will actually use right now, given the keys that are set. */
	@Transactional(readOnly = true)
	public Reviewer effectiveReviewer() {
		boolean claude = apiKey(Provider.CLAUDE).isPresent();
		boolean gemini = apiKey(Provider.GEMINI).isPresent();
		return switch (reviewer()) {
			case AUTO -> claude ? Reviewer.CLAUDE : gemini ? Reviewer.GEMINI : Reviewer.OFFLINE;
			case CLAUDE -> claude ? Reviewer.CLAUDE : Reviewer.OFFLINE;
			case GEMINI -> gemini ? Reviewer.GEMINI : Reviewer.OFFLINE;
			case BOTH -> claude && gemini ? Reviewer.BOTH : claude ? Reviewer.CLAUDE : gemini ? Reviewer.GEMINI : Reviewer.OFFLINE;
			case OFFLINE -> Reviewer.OFFLINE;
		};
	}

	@Transactional(readOnly = true)
	public View view() {
		return new View(reviewer().name().toLowerCase(Locale.ROOT), effectiveReviewer().name().toLowerCase(Locale.ROOT),
				provider(Provider.CLAUDE), provider(Provider.GEMINI), cipherKey != null);
	}

	@Transactional
	public View update(Update u) {
		if (u.reviewer() != null) write("review.mode", Reviewer.valueOf(u.reviewer().toUpperCase(Locale.ROOT)).name());
		key(Provider.CLAUDE, u.claudeKey());
		key(Provider.GEMINI, u.geminiKey());
		model(Provider.CLAUDE, u.claudeModel());
		model(Provider.GEMINI, u.geminiModel());
		if (u.geminiFallbacks() != null) {
			List<String> list = split(u.geminiFallbacks());
			if (list.isEmpty()) repo.deleteById("gemini.fallbacks");
			else write("gemini.fallbacks", String.join(",", list));
		}
		return view();
	}

	private void key(Provider p, String key) {
		if (key == null) return;
		if (key.isBlank()) repo.deleteById(p.id + ".key");
		else write(p.id + ".key", key.trim());
	}

	private void model(Provider p, String model) {
		if (model == null) return;
		if (model.isBlank()) repo.deleteById(p.id + ".model");
		else write(p.id + ".model", model.trim());
	}

	private ProviderView provider(Provider p) {
		Optional<String> saved = read(p.id + ".key");
		Optional<String> key = apiKey(p);
		String hint = key.map(k -> k.length() > 8 ? "…" + k.substring(k.length() - 4) : "set").orElse(null);
		String source = saved.isPresent() ? "settings" : key.isPresent() ? "environment" : null;
		return new ProviderView(key.isPresent(), hint, source, model(p),
				p == Provider.CLAUDE ? claudeDefault : geminiDefault, fallbacks(p),
				p == Provider.CLAUDE ? List.of() : geminiFallbacksDefault);
	}

	private Optional<String> read(String name) {
		return repo.findById(name).map(AppSetting::getValue).map(this::open).filter(s -> !s.isBlank());
	}

	private void write(String name, String value) {
		repo.save(new AppSetting(name, seal(value)));
	}

	private String seal(String value) {
		if (cipherKey == null) return PLAIN + value;
		try {
			byte[] iv = new byte[12];
			random.nextBytes(iv);
			Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
			c.init(Cipher.ENCRYPT_MODE, cipherKey, new GCMParameterSpec(128, iv));
			byte[] ct = c.doFinal(value.getBytes(StandardCharsets.UTF_8));
			return ENC + Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array());
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Could not encrypt the setting", e);
		}
	}

	private String open(String stored) {
		if (stored.startsWith(PLAIN)) return stored.substring(PLAIN.length());
		if (!stored.startsWith(ENC) || cipherKey == null) return "";
		try {
			byte[] all = Base64.getDecoder().decode(stored.substring(ENC.length()));
			Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
			c.init(Cipher.DECRYPT_MODE, cipherKey, new GCMParameterSpec(128, all, 0, 12));
			return new String(c.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException e) {
			// The secret changed: the saved key can't be read, so it counts as not set.
			return "";
		}
	}

	private static byte[] sha256(String s) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
