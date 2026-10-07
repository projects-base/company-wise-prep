package com.companywiseprep.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Single-user password login for the hosted app (prep.auth.*).
 *
 * The password is compared in constant time; a success returns a token
 * {@code <expiry-epoch-seconds>.<HMAC-SHA256>} signed with a server secret. Stateless, so a
 * restart doesn't log anyone out, and changing APP_TOKEN_SECRET logs every device out.
 * Locally (prep.auth.enabled=false) none of this applies.
 */
@Service
public class AuthService {

	static final Duration TOKEN_TTL = Duration.ofDays(30);
	/** Failed attempts allowed per window before logins are refused for the rest of it. */
	static final int MAX_FAILURES = 5;
	static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);

	private final boolean enabled;
	private final byte[] password;
	private final byte[] secret;
	private final Clock clock;
	private final Deque<Instant> failures = new ArrayDeque<>();

	public AuthService(@Value("${prep.auth.enabled:false}") boolean enabled,
			@Value("${prep.auth.password:}") String password,
			@Value("${prep.auth.token-secret:}") String secret, Clock clock) {
		this.enabled = enabled;
		this.password = password.getBytes(StandardCharsets.UTF_8);
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.clock = clock;
		if (enabled && (password.length() < 12 || secret.length() < 32)) {
			throw new IllegalStateException("prep.auth.enabled needs APP_PASSWORD (12+ chars) and "
					+ "APP_TOKEN_SECRET (32+ chars) — refusing to start an unprotected code runner");
		}
	}

	public boolean enabled() {
		return enabled;
	}

	public enum LoginResult {
		OK, WRONG, LOCKED
	}

	public record Login(LoginResult result, String token, Instant expires) {
	}

	public synchronized Login login(String attempt) {
		Instant now = clock.instant();
		while (!failures.isEmpty() && failures.peekFirst().isBefore(now.minus(FAILURE_WINDOW))) failures.pollFirst();
		if (failures.size() >= MAX_FAILURES) return new Login(LoginResult.LOCKED, null, null);
		byte[] given = attempt == null ? new byte[0] : attempt.getBytes(StandardCharsets.UTF_8);
		if (!MessageDigest.isEqual(sha256(given), sha256(password))) {
			failures.addLast(now);
			return new Login(LoginResult.WRONG, null, null);
		}
		failures.clear();
		Instant expires = now.plus(TOKEN_TTL);
		String payload = Long.toString(expires.getEpochSecond());
		return new Login(LoginResult.OK, payload + "." + sign(payload), expires);
	}

	public boolean valid(String token) {
		if (token == null) return false;
		int dot = token.indexOf('.');
		if (dot <= 0) return false;
		String payload = token.substring(0, dot);
		byte[] expected = sign(payload).getBytes(StandardCharsets.US_ASCII);
		if (!MessageDigest.isEqual(expected, token.substring(dot + 1).getBytes(StandardCharsets.US_ASCII))) return false;
		try {
			return Instant.ofEpochSecond(Long.parseLong(payload)).isAfter(clock.instant());
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String sign(String payload) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.length == 0 ? new byte[1] : secret, "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static byte[] sha256(byte[] b) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(b);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
