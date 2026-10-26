package com.companywiseprep.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companywiseprep.academy.AcademyService;
import com.companywiseprep.bank.BankService;
import com.companywiseprep.campaign.CampaignConfig;
import com.companywiseprep.campaign.CampaignService;
import com.companywiseprep.data.DataImporter;
import com.companywiseprep.progress.ProgressService;
import com.companywiseprep.review.AnswerReview;
import com.companywiseprep.review.AnswerReviewService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final BankService bank;
	private final CampaignService campaigns;
	private final ProgressService progress;
	private final DataImporter importer;
	private final AcademyService academy;
	private final AnswerReviewService reviews;

	public ApiController(BankService bank, CampaignService campaigns, ProgressService progress,
			DataImporter importer, AcademyService academy, AnswerReviewService reviews) {
		this.bank = bank;
		this.campaigns = campaigns;
		this.progress = progress;
		this.importer = importer;
		this.academy = academy;
		this.reviews = reviews;
	}

	@GetMapping("/companies")
	public List<BankService.CompanySummary> companies() {
		return bank.companies();
	}

	@GetMapping("/companies/{slug}")
	public BankService.CompanyView company(@PathVariable String slug) {
		return bank.company(slug);
	}

	@GetMapping("/questions")
	public List<BankService.QuestionSummary> questions() {
		return bank.questions();
	}

	@GetMapping("/questions/{slug}")
	public BankService.QuestionView question(@PathVariable String slug) {
		return bank.question(slug);
	}

	public record ProgressRequest(@NotNull ProgressService.Action action, String notes) {
	}

	@PutMapping("/questions/{slug}/progress")
	public ProgressService.View updateProgress(@PathVariable String slug, @Valid @RequestBody ProgressRequest req) {
		return progress.apply(slug, req.action(), req.notes());
	}

	public record ReviewRequest(@NotBlank @Size(max = 10_000) String answer) {
	}

	/** Judges a theory answer (spoken or typed) against the model answer. */
	@PostMapping("/questions/{slug}/review")
	public AnswerReview review(@PathVariable String slug, @Valid @RequestBody ReviewRequest req) {
		return reviews.review(slug, req.answer());
	}

	/** Past review scores for a question, oldest first. */
	@GetMapping("/questions/{slug}/reviews")
	public List<Double> reviewHistory(@PathVariable String slug) {
		return reviews.history(slug);
	}

	@GetMapping("/campaigns")
	public List<CampaignConfig> campaigns() {
		return campaigns.list();
	}

	@GetMapping("/campaigns/{id}/plan")
	public CampaignService.PlanResponse plan(@PathVariable String id) {
		return campaigns.plan(id);
	}

	@GetMapping("/campaigns/{id}/today")
	public CampaignService.Today today(@PathVariable String id) {
		return campaigns.today(id);
	}

	@GetMapping("/academy")
	public AcademyService.Overview academy() {
		return academy.overview();
	}

	@GetMapping("/academy/modules/{id}")
	public AcademyService.ModuleView module(@PathVariable String id) {
		return academy.module(id);
	}

	public record LessonProgressRequest(boolean done) {
	}

	@PutMapping("/academy/modules/{id}/progress")
	public AcademyService.ModuleSummary moduleProgress(@PathVariable String id, @RequestBody LessonProgressRequest req) {
		return academy.setDone(id, req.done());
	}

	/** Re-read data/ after /prep-company or a manual edit, without restarting. */
	@PostMapping("/admin/reload")
	public DataImporter.Summary reload() {
		return importer.importAll();
	}
}
