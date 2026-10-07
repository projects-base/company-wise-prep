package com.companywiseprep.bank;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.code.CodeChallengeRepository;
import com.companywiseprep.data.Yaml;
import com.companywiseprep.progress.ProgressService;
import com.companywiseprep.web.NotFoundException;

@Service
public class BankService {

	private final CompanyRepository companies;
	private final QuestionRepository questions;
	private final ProgressService progress;
	private final CodeChallengeRepository challenges;

	public BankService(CompanyRepository companies, QuestionRepository questions, ProgressService progress,
			CodeChallengeRepository challenges) {
		this.companies = companies;
		this.questions = questions;
		this.progress = progress;
		this.challenges = challenges;
	}

	public record CompanySummary(String slug, String name, String researchedOn, long questions) {
	}

	public record CompanyView(String slug, String name, String researchedOn, Map<String, Object> data,
			String dossier) {
	}

	public record QuestionSummary(String slug, String title, String type, String difficulty, List<String> tags,
			String leetcode, List<String> companies, int sightings, String lastSeen, boolean done, boolean starred,
			boolean runnable) {
	}

	public record SightingView(String company, String role, String round, String seenOn, String confidence,
			String source) {
	}

	public record QuestionView(String slug, String title, String type, String difficulty, List<String> tags,
			String leetcode, String prompt, List<String> followUps, List<String> academy,
			List<SightingView> sightings, ProgressService.View progress, boolean runnable) {
	}

	@Transactional(readOnly = true)
	public List<CompanySummary> companies() {
		List<Question> all = questions.findAllWithSightings();
		return companies.findAll().stream()
				.sorted(Comparator.comparing(Company::getName))
				.map(c -> new CompanySummary(c.getSlug(), c.getName(), c.getResearchedOn(),
						all.stream().filter(q -> askedAt(q, c.getSlug())).count()))
				.toList();
	}

	@Transactional(readOnly = true)
	public CompanyView company(String slug) {
		Company c = companies.findById(slug).orElseThrow(() -> new NotFoundException("No company " + slug));
		return new CompanyView(c.getSlug(), c.getName(), c.getResearchedOn(), Yaml.load(c.getYaml()), c.getDossier());
	}

	/** The whole bank; the UI filters it. */
	@Transactional(readOnly = true)
	public List<QuestionSummary> questions() {
		Map<String, ProgressService.View> prog = progress.all();
		java.util.Set<String> runnable = new java.util.HashSet<>(challenges.findAllSlugs());
		return questions.findAllWithSightings().stream()
				.map(q -> {
					ProgressService.View p = prog.getOrDefault(q.getSlug(), ProgressService.View.EMPTY);
					return new QuestionSummary(q.getSlug(), q.getTitle(), q.getType(), q.getDifficulty(),
							split(q.getTags(), ","), q.getLeetcode(), companiesOf(q), q.getSightings().size(),
							q.getSightings().stream().map(Sighting::getSeenOn).filter(s -> s != null)
									.max(Comparator.naturalOrder()).orElse(null),
							p.done(), p.starred(), runnable.contains(q.getSlug()));
				})
				.sorted(Comparator.comparing(QuestionSummary::sightings).reversed()
						.thenComparing(QuestionSummary::title))
				.toList();
	}

	@Transactional(readOnly = true)
	public QuestionView question(String slug) {
		Question q = questions.findWithSightings(slug).orElseThrow(() -> new NotFoundException("No question " + slug));
		return new QuestionView(q.getSlug(), q.getTitle(), q.getType(), q.getDifficulty(), split(q.getTags(), ","),
				q.getLeetcode(), q.getPrompt(), split(q.getFollowUps(), "\n"), split(q.getAcademy(), ","),
				q.getSightings().stream()
						.map(s -> new SightingView(s.getCompany(), s.getRole(), s.getRound(), s.getSeenOn(),
								s.getConfidence(), s.getSource()))
						.toList(),
				progress.of(slug), challenges.existsById(slug));
	}

	static List<String> companiesOf(Question q) {
		return q.getSightings().stream().map(Sighting::getCompany).distinct().sorted().toList();
	}

	private static boolean askedAt(Question q, String company) {
		return q.getSightings().stream().anyMatch(s -> company.equals(s.getCompany()));
	}

	private static List<String> split(String s, String sep) {
		return s == null || s.isBlank() ? List.of()
				: Arrays.stream(s.split(sep)).map(String::trim).filter(x -> !x.isEmpty())
						.collect(Collectors.toList());
	}
}
