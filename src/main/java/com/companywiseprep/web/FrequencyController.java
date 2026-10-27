package com.companywiseprep.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.bank.BankService;
import com.companywiseprep.frequency.FrequencyService;
import com.companywiseprep.frequency.FrequencyService.FrequencyException;
import com.companywiseprep.frequency.FrequencyService.ListView;
import com.companywiseprep.frequency.FrequencyService.Tag;
import com.companywiseprep.frequency.FrequencyService.Window;

/** LeetCode company-tag frequency lists, read live from GitHub (see FrequencyService). */
@RestController
@RequestMapping("/api")
public class FrequencyController {

	private final FrequencyService frequency;
	private final BankService bank;

	public FrequencyController(FrequencyService frequency, BankService bank) {
		this.frequency = frequency;
		this.bank = bank;
	}

	@GetMapping("/frequency/folders")
	public List<String> folders() {
		return frequency.folders();
	}

	@GetMapping("/frequency/lists")
	public ListView list(@RequestParam String folder, @RequestParam(required = false) String window) {
		return frequency.list(folder, Window.parse(window));
	}

	/** {@code folder} is null when the repo has no list for this company. */
	public record CompanyFrequency(String folder, ListView list) {
	}

	@GetMapping("/companies/{slug}/frequency")
	public CompanyFrequency forCompany(@PathVariable String slug, @RequestParam(required = false) String folder,
			@RequestParam(required = false) String window) {
		String f = folder != null && !folder.isBlank() ? folder : frequency.folderFor(slug).orElse(null);
		return new CompanyFrequency(f, f == null ? null : frequency.list(f, Window.parse(window)));
	}

	public record Hot(List<String> companies, Map<String, List<Tag>> tags) {
	}

	/** Six-month tags from the big product companies, keyed by bank question slug (bank problems only). */
	@GetMapping("/frequency/hot")
	public Hot hot() {
		Map<String, List<Tag>> byLeetcode = frequency.hot();
		Map<String, List<Tag>> tags = new HashMap<>();
		bank.questions().forEach(q -> {
			List<Tag> t = q.leetcode() == null ? null : byLeetcode.get(q.leetcode());
			if (t != null) tags.put(q.slug(), t);
		});
		return new Hot(frequency.hotCompanies(), tags);
	}

	public record Problem(String error) {
	}

	@ExceptionHandler(FrequencyException.class)
	public ResponseEntity<Problem> unavailable(FrequencyException e) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new Problem(e.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Problem> badWindow(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(new Problem("Unknown window; use thirty-days, three-months, six-months, older or all."));
	}
}
