package com.companywiseprep.code;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.progress.ProgressService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON-only on purpose: a cross-site form can't send application/json without a CORS preflight. */
@RestController
@RequestMapping("/api")
public class CodeController {

	static final int MAX_CODE = 100_000;

	private final JudgeService judge;
	private final ProgressService progress;

	public CodeController(JudgeService judge, ProgressService progress) {
		this.judge = judge;
		this.progress = progress;
	}

	@GetMapping("/code/{slug}")
	public JudgeService.ChallengeView challenge(@PathVariable String slug) {
		return judge.view(slug);
	}

	@GetMapping("/code/{slug}/solution")
	public Map<String, String> solution(@PathVariable String slug) {
		return Map.of("code", judge.reference(slug));
	}

	public record RunRequest(@NotNull @Size(max = MAX_CODE) String code, @NotNull JudgeService.Mode mode,
			@Size(max = 1_000_000) String customInput) {
	}

	@PostMapping(path = "/code/{slug}/run", consumes = MediaType.APPLICATION_JSON_VALUE)
	public JudgeService.JudgeResult run(@PathVariable String slug, @Valid @RequestBody RunRequest req) {
		return judge.judge(slug, req.code(), req.mode(), req.customInput());
	}

	public record DraftRequest(@NotNull @Size(max = MAX_CODE) String code) {
	}

	@PutMapping(path = "/code/{slug}/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void draft(@PathVariable String slug, @Valid @RequestBody DraftRequest req) {
		progress.saveCode(slug, req.code());
	}

	public record PlaygroundRequest(@NotNull @Size(max = MAX_CODE) String code, @Size(max = 1_000_000) String stdin) {
	}

	@PostMapping(path = "/playground/run", consumes = MediaType.APPLICATION_JSON_VALUE)
	public JudgeService.PlaygroundResult playground(@Valid @RequestBody PlaygroundRequest req) {
		return judge.playground(req.code(), req.stdin());
	}
}
