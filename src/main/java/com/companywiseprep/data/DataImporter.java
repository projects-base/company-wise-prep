package com.companywiseprep.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.academy.AcademyModule;
import com.companywiseprep.academy.AcademyModuleRepository;
import com.companywiseprep.academy.AcademyPath;
import com.companywiseprep.academy.AcademyPathRepository;
import com.companywiseprep.academy.AcademyTrack;
import com.companywiseprep.academy.AcademyTrackRepository;
import com.companywiseprep.bank.Company;
import com.companywiseprep.bank.CompanyRepository;
import com.companywiseprep.bank.Question;
import com.companywiseprep.bank.QuestionRepository;
import com.companywiseprep.bank.Sighting;
import com.companywiseprep.bank.SightingRepository;
import com.companywiseprep.campaign.Campaign;
import com.companywiseprep.campaign.CampaignRepository;
import com.companywiseprep.code.CodeChallenge;
import com.companywiseprep.code.CodeChallengeRepository;

import jakarta.persistence.EntityManager;

/**
 * Loads data/ into the database on start-up and on demand.
 *
 * The files are the source of truth for content, so an import replaces every content table.
 * Progress is keyed by slug in its own table and is never touched here.
 */
@Component
public class DataImporter implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataImporter.class);

	private final Path dataDir;
	private final CompanyRepository companies;
	private final QuestionRepository questions;
	private final SightingRepository sightings;
	private final CampaignRepository campaigns;
	private final AcademyTrackRepository tracks;
	private final AcademyModuleRepository modules;
	private final AcademyPathRepository paths;
	private final CodeChallengeRepository challenges;
	private final ImportStateRepository state;
	private final EntityManager em;

	public DataImporter(@Value("${prep.data-dir}") Path dataDir, CompanyRepository companies,
			QuestionRepository questions, SightingRepository sightings, CampaignRepository campaigns,
			AcademyTrackRepository tracks, AcademyModuleRepository modules, AcademyPathRepository paths,
			CodeChallengeRepository challenges, ImportStateRepository state, EntityManager em) {
		this.dataDir = dataDir;
		this.companies = companies;
		this.questions = questions;
		this.sightings = sightings;
		this.campaigns = campaigns;
		this.tracks = tracks;
		this.modules = modules;
		this.paths = paths;
		this.challenges = challenges;
		this.state = state;
		this.em = em;
	}

	public record Summary(int companies, int questions, int sightings, int campaigns, int modules, int lessons,
			int challenges) {
	}

	/**
	 * Transactional itself: calling importAll() from here is a self-invocation, which skips the
	 * proxy, so importAll()'s own @Transactional would never apply (CURRICULUM D3).
	 */
	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		String fingerprint = fingerprint();
		ImportState last = state.findById(ImportState.ID).orElse(null);
		if (last != null && fingerprint.equals(last.getFingerprint()) && questions.count() > 0) {
			log.info("data/ unchanged since {} — skipping import (POST /api/admin/reload forces one)",
					last.getImportedAt());
			return;
		}
		Summary s = importAll();
		log.info("Imported {} companies, {} questions, {} sightings, {} campaigns, {} academy modules ({} with lessons), "
				+ "{} code challenges from {}", s.companies(), s.questions(), s.sightings(), s.campaigns(), s.modules(),
				s.lessons(), s.challenges(), dataDir.toAbsolutePath());
	}

	@Transactional
	public Summary importAll() {
		if (!Files.isDirectory(dataDir)) {
			throw new IllegalStateException("Data directory not found: " + dataDir.toAbsolutePath()
					+ " (start the app from the repo root or set prep.data-dir)");
		}
		// Read everything first: a malformed file aborts before anything is deleted.
		List<Company> newCompanies = readCompanies();
		List<Question> newQuestions = readQuestions();
		List<Campaign> newCampaigns = readCampaigns();
		Academy academy = readAcademy();
		List<CodeChallenge> newChallenges = readChallenges();

		// Bulk deletes run immediately; the flush stops Hibernate ordering the replacement
		// inserts ahead of them, where same-id rows would collide on a re-import.
		sightings.deleteAllInBatch();
		questions.deleteAllInBatch();
		companies.deleteAllInBatch();
		campaigns.deleteAllInBatch();
		tracks.deleteAllInBatch();
		modules.deleteAllInBatch();
		paths.deleteAllInBatch();
		challenges.deleteAllInBatch();
		em.flush();
		em.clear();

		companies.saveAll(newCompanies);
		questions.saveAll(newQuestions);
		campaigns.saveAll(newCampaigns);
		tracks.saveAll(academy.tracks());
		modules.saveAll(academy.modules());
		paths.saveAll(academy.paths());
		challenges.saveAll(newChallenges);

		ImportState done = state.findById(ImportState.ID).orElseGet(ImportState::new);
		done.setFingerprint(fingerprint());
		done.setImportedAt(java.time.Instant.now());
		state.save(done);

		int sightingCount = newQuestions.stream().mapToInt(q -> q.getSightings().size()).sum();
		int lessons = (int) academy.modules().stream().filter(m -> !m.getLesson().isBlank()).count();
		return new Summary(newCompanies.size(), newQuestions.size(), sightingCount, newCampaigns.size(),
				academy.modules().size(), lessons, newChallenges.size());
	}

	private List<Company> readCompanies() {
		List<Company> out = new ArrayList<>();
		for (Path dir : list(dataDir.resolve("companies"), Files::isDirectory)) {
			Path yamlFile = dir.resolve("company.yaml");
			if (!Files.exists(yamlFile)) continue;
			String text = read(yamlFile);
			Map<String, Object> y = Yaml.load(text);
			Company c = new Company();
			c.setSlug(Yaml.str(y.getOrDefault("slug", dir.getFileName().toString())));
			c.setName(Yaml.str(y.getOrDefault("name", c.getSlug())));
			c.setResearchedOn(Yaml.str(y.get("researched_on")));
			c.setYaml(text);
			Path dossier = dir.resolve("dossier.md");
			c.setDossier(Files.exists(dossier) ? read(dossier) : "");
			out.add(c);
		}
		return out;
	}

	private List<Question> readQuestions() {
		List<Question> out = new ArrayList<>();
		for (Path typeDir : list(dataDir.resolve("questions"), Files::isDirectory)) {
			for (Path f : list(typeDir, p -> p.toString().endsWith(".yaml"))) {
				Map<String, Object> y = Yaml.load(read(f));
				Question q = new Question();
				q.setSlug(Yaml.str(y.get("slug")));
				q.setTitle(Yaml.str(y.get("title")));
				q.setType(Yaml.str(y.get("type")));
				q.setDifficulty(Yaml.str(y.get("difficulty")));
				q.setTags(String.join(",", Yaml.strings(y.get("tags"))));
				q.setLeetcode(Yaml.str(y.get("leetcode")));
				q.setPrompt(Yaml.str(y.get("prompt")));
				q.setFollowUps(String.join("\n", Yaml.strings(y.get("follow_ups"))));
				q.setAcademy(String.join(",", Yaml.strings(y.get("academy"))));
				for (Map<String, Object> s : Yaml.maps(y.get("sightings"))) {
					Sighting si = new Sighting();
					si.setCompany(Yaml.str(s.get("company")));
					si.setRole(Yaml.str(s.get("role")));
					si.setRound(Yaml.str(s.get("round")));
					si.setSeenOn(Yaml.str(s.get("seen_on")));
					si.setConfidence(Yaml.str(s.getOrDefault("confidence", "claimed")));
					si.setSource(Yaml.str(s.get("source")));
					q.addSighting(si);
				}
				out.add(q);
			}
		}
		return out;
	}

	private List<Campaign> readCampaigns() {
		List<Campaign> out = new ArrayList<>();
		for (Path dir : list(dataDir.resolve("campaigns"), Files::isDirectory)) {
			Path f = dir.resolve("campaign.yaml");
			if (!Files.exists(f)) continue;
			String text = read(f);
			Map<String, Object> y = Yaml.load(text);
			Campaign c = new Campaign();
			c.setId(Yaml.str(y.getOrDefault("id", dir.getFileName().toString())));
			c.setName(Yaml.str(y.getOrDefault("name", c.getId())));
			c.setYaml(text);
			out.add(c);
		}
		return out;
	}

	private record Academy(List<AcademyTrack> tracks, List<AcademyModule> modules, List<AcademyPath> paths) {
	}

	/** data/academy/curriculum.yaml plus one lessons/<id>.md per module (missing = not written yet). */
	private Academy readAcademy() {
		Path dir = dataDir.resolve("academy");
		Path curriculum = dir.resolve("curriculum.yaml");
		if (!Files.exists(curriculum)) return new Academy(List.of(), List.of(), List.of());
		Map<String, Object> y = Yaml.load(read(curriculum));
		List<AcademyTrack> trackList = new ArrayList<>();
		List<AcademyModule> moduleList = new ArrayList<>();
		int t = 0;
		for (Map<String, Object> ty : Yaml.maps(y.get("tracks"))) {
			AcademyTrack track = new AcademyTrack();
			track.setId(Yaml.str(ty.get("id")));
			track.setTitle(Yaml.str(ty.get("title")));
			track.setDescription(Yaml.str(ty.get("description")));
			track.setOrdinal(t++);
			trackList.add(track);
			int m = 0;
			for (Map<String, Object> my : Yaml.maps(ty.get("modules"))) {
				AcademyModule mod = new AcademyModule();
				mod.setId(Yaml.str(my.get("id")));
				mod.setTrackId(track.getId());
				mod.setTitle(Yaml.str(my.get("title")));
				mod.setLevel(my.get("level") instanceof Number n ? n.intValue() : 1);
				mod.setMinutes(my.get("minutes") instanceof Number n ? n.intValue() : 25);
				mod.setPrerequisites(String.join(",", Yaml.strings(my.get("prerequisites"))));
				mod.setOrdinal(m++);
				Path lesson = dir.resolve("lessons").resolve(mod.getId() + ".md");
				mod.setLesson(Files.exists(lesson) ? read(lesson) : "");
				moduleList.add(mod);
			}
		}
		List<AcademyPath> pathList = new ArrayList<>();
		int p = 0;
		for (Map<String, Object> py : Yaml.maps(y.get("paths"))) {
			AcademyPath path = new AcademyPath();
			path.setId(Yaml.str(py.get("id")));
			path.setTitle(Yaml.str(py.get("title")));
			path.setDescription(Yaml.str(py.get("description")));
			path.setModules(String.join(",", Yaml.strings(py.get("modules"))));
			path.setOrdinal(p++);
			pathList.add(path);
		}
		return new Academy(trackList, moduleList, pathList);
	}

	/** data/code/<slug>/ — only complete challenges; a half-written one is skipped, not fatal. */
	private List<CodeChallenge> readChallenges() {
		List<CodeChallenge> out = new ArrayList<>();
		for (Path dir : list(dataDir.resolve("code"), Files::isDirectory)) {
			Path problem = dir.resolve("problem.md"), starter = dir.resolve("Solution.java"),
					harness = dir.resolve("Main.java"), reference = dir.resolve("reference").resolve("Solution.java"),
					tests = dir.resolve("tests.yaml");
			if (!Stream.of(problem, starter, harness, reference, tests).allMatch(Files::exists)) {
				log.warn("Skipping incomplete code challenge {}", dir.getFileName());
				continue;
			}
			String testsText = read(tests);
			CodeChallenge c = new CodeChallenge();
			c.setSlug(dir.getFileName().toString());
			c.setMethod(Yaml.str(Yaml.load(testsText).get("method")));
			c.setProblem(read(problem));
			c.setStarter(read(starter));
			c.setHarness(read(harness));
			c.setReference(read(reference));
			c.setTests(testsText);
			out.add(c);
		}
		return out;
	}

	/**
	 * SHA-256 over every file under data/ (path and bytes, in sorted order), skipping staging.
	 * Content-based rather than timestamps, so a fresh git clone of the same data still matches.
	 */
	String fingerprint() {
		try (Stream<Path> walk = Files.walk(dataDir)) {
			java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
			for (Path f : walk.filter(Files::isRegularFile)
					.filter(f -> !dataDir.relativize(f).toString().replace('\\', '/').startsWith("_staging/"))
					.sorted().toList()) {
				sha.update(dataDir.relativize(f).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
				sha.update((byte) 0);
				sha.update(Files.readAllBytes(f));
			}
			return java.util.HexFormat.of().formatHex(sha.digest());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Directory entries, sorted, skipping "_"-prefixed ones (staging, templates). */
	private static List<Path> list(Path dir, Predicate<Path> filter) {
		if (!Files.isDirectory(dir)) return List.of();
		try (Stream<Path> s = Files.list(dir)) {
			return s.filter(filter).filter(p -> !p.getFileName().toString().startsWith("_"))
					.sorted().toList();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String read(Path f) {
		try {
			return Files.readString(f, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot read " + f, e);
		}
	}
}
