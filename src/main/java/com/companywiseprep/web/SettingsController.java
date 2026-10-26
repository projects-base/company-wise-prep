package com.companywiseprep.web;

import java.util.List;
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

	public record TestResult(boolean ok, String model, String message, List<GeminiGateway.ModelCheck> checks) {
	}

	/**
	 * Sends one tiny request with the saved key, so a wrong key or model shows up here, not
	 * mid-practice. For Gemini every model in the fallback chain is checked.
	 */
	@PostMapping("/test/{provider}")
	public TestResult test(@PathVariable String provider) {
		if (provider.toLowerCase(Locale.ROOT).startsWith("claude")) {
			try {
				return new TestResult(true, claude.model(), "Connected. The model replied: " + claude.ping(), null);
			} catch (RuntimeException e) {
				return new TestResult(false, claude.model(), e.getMessage(), null);
			}
		}
		try {
			List<GeminiGateway.ModelCheck> checks = gemini.ping();
			long ok = checks.stream().filter(GeminiGateway.ModelCheck::ok).count();
			String message = checks.get(0).ok() ? "Connected. " + ok + " of " + checks.size() + " models answered."
					: ok > 0 ? "The main model failed, but " + ok + " fallback model(s) answered, so reviews will still work."
					: "No model answered: " + checks.get(0).message();
			return new TestResult(ok > 0, gemini.model(), message, checks);
		} catch (RuntimeException e) {
			return new TestResult(false, gemini.model(), e.getMessage(), null);
		}
	}
}
