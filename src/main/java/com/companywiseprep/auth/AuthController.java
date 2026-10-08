package com.companywiseprep.auth;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService auth;
	private final boolean codeRunner;

	public AuthController(AuthService auth,
			@org.springframework.beans.factory.annotation.Value("${prep.code-runner.enabled:true}") boolean codeRunner) {
		this.auth = auth;
		this.codeRunner = codeRunner;
	}

	/**
	 * Public: tells the UI whether to show a login screen and which features this server has.
	 * Also Render's health check.
	 */
	public record Status(boolean authRequired, boolean authenticated, boolean codeRunner) {
	}

	@GetMapping("/status")
	public Status status(@RequestHeader(name = "Authorization", required = false) String header) {
		String token = header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
		return new Status(auth.enabled(), !auth.enabled() || auth.valid(token), codeRunner);
	}

	public record LoginRequest(String password) {
	}

	public record LoginResponse(String token, Instant expires) {
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		if (!auth.enabled()) return ResponseEntity.ok(new LoginResponse("", null));
		AuthService.Login result = auth.login(req.password());
		return switch (result.result()) {
			case OK -> ResponseEntity.ok(new LoginResponse(result.token(), result.expires()));
			case WRONG -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "wrong password"));
			case LOCKED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body(java.util.Map.of("error", "too many attempts — wait 10 minutes"));
		};
	}
}
