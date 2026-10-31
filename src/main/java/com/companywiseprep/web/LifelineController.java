package com.companywiseprep.web;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.lifeline.LifelineService;
import com.companywiseprep.lifeline.LifelineService.Help;
import com.companywiseprep.lifeline.LifelineService.Kind;
import com.companywiseprep.lifeline.LifelineService.Turn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Ask AI: hints and doubts on the question or lesson being studied, or any doubt from any other page. */
@RestController
@RequestMapping("/api/lifeline")
public class LifelineController {

	private final LifelineService lifeline;

	public LifelineController(LifelineService lifeline) {
		this.lifeline = lifeline;
	}

	public record HelpRequest(
			@NotNull @Pattern(regexp = "question|lesson|general") String context,
			@NotBlank @Size(max = 200) String ref,
			@NotNull Kind kind,
			@Size(max = 2_000) String doubt,
			@Size(max = 20) List<Turn> history) {
	}

	@PostMapping
	public Help help(@Valid @RequestBody HelpRequest req) {
		return lifeline.help(req.context(), req.ref(), req.kind(), req.doubt(), req.history());
	}
}
