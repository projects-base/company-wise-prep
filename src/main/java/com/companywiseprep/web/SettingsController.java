package com.companywiseprep.web;

import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.claude.ClaudeGateway;
import com.companywiseprep.gemini.GeminiGateway;
import com.companywiseprep.settings.SettingsService;

/** The Settings page: AI reviewer keys (write-only), models and which reviewer judges answers. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

	private final SettingsService settings;
	private final ClaudeGateway claude;
	private final GeminiGateway gemini;

	public SettingsController(SettingsService settings, ClaudeGateway claude, GeminiGateway gemini) {
		this.settings = settings;
		this.claude = claude;
		this.gemini = gemini;
	}

	@GetMapping
	public SettingsService.View get() {
		return settings.view();
	}

	@PutMapping
	public SettingsService.View update(@RequestBody SettingsService.Update update) {
		return settings.update(update);
	}

	public record TestResult(boolean ok, String model, String message) {
	}

	/** Sends one tiny request with the saved key, so a wrong key or model shows up here, not mid-practice. */
	@PostMapping("/test/{provider}")
	public TestResult test(@PathVariable String provider) {
		boolean isClaude = provider.toLowerCase(Locale.ROOT).startsWith("claude");
		String model = isClaude ? claude.model() : gemini.model();
		try {
			String reply = isClaude ? claude.ping() : gemini.ping();
			return new TestResult(true, model, "Connected. The model replied: " + reply);
		} catch (RuntimeException e) {
			return new TestResult(false, model, e.getMessage());
		}
	}
}
