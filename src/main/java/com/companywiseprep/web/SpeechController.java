package com.companywiseprep.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.gemini.GeminiGateway;
import com.companywiseprep.gemini.GeminiGateway.GeminiException;

/** Speech to text through Gemini, for when the browser's own speech service can't be used. */
@RestController
public class SpeechController {

	// About 7 minutes of 16 kHz mono WAV; Gemini takes up to 20 MB inline.
	private static final int MAX_BYTES = 15 * 1024 * 1024;

	private final GeminiGateway gemini;

	public SpeechController(GeminiGateway gemini) {
		this.gemini = gemini;
	}

	public record Transcript(String text, String error) {
	}

	@PostMapping(path = "/api/transcribe", consumes = { "audio/wav", "audio/x-wav" })
	public ResponseEntity<Transcript> transcribe(@RequestBody byte[] audio) {
		if (audio.length == 0 || audio.length > MAX_BYTES) {
			return ResponseEntity.badRequest().body(new Transcript(null, "The recording is empty or too long (max about 7 minutes)."));
		}
		try {
			return ResponseEntity.ok(new Transcript(gemini.transcribe(audio, "audio/wav"), null));
		} catch (GeminiException e) {
			return ResponseEntity.ok(new Transcript(null, e.getMessage()));
		}
	}
}
