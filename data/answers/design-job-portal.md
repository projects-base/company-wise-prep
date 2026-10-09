**Short answer:** Core entities are `Recruiter`, `Job`, `Candidate` (with a `Profile` of skills and years of experience) and `Application`. An eligibility rule decides whether a candidate can apply to a job, and a pluggable `MatchScorer` gives a score for a (candidate, job) pair. "Top candidates for a job" scores the job's applicants and keeps the top K with a min-heap; "top eligible jobs for a candidate" filters open jobs by eligibility, scores them, and does the same. Repositories are in-memory maps so the code runs as-is.

## Requirements

- Recruiters post jobs: title, required skills, min and max YOE, status open or closed.
- Candidates have profiles: skills, YOE.
- Candidates apply to jobs; one application per (candidate, job).
- `topCandidates(jobId, k)`: best applicants for a job.
- `topJobs(candidateId, k)`: best open jobs the candidate is eligible for.
- Executable code, in memory. Out of scope: auth, search by text, persistence.

Assumed scoring: skill overlap (fraction of required skills the candidate has) weighted most, with a small bonus for YOE closeness. Eligibility: YOE in range and at least one required skill.

## Classes

- `Recruiter`, `Candidate`, `Job`, `Application`: records or small classes, data only.
- `Skill`: a normalized string (lower-case) wrapped in a record, so "Java" and "java" match.
- `EligibilityRule` (interface): `boolean isEligible(Candidate, Job)`. Composable with `and`.
- `MatchScorer` (interface): `double score(Candidate, Job)`.
- `JobRepository`, `CandidateRepository`, `ApplicationRepository`: in-memory maps.
- `JobPortalService`: use cases (`postJob`, `apply`, `topCandidates`, `topJobs`).

## Patterns used

- **Strategy**: `MatchScorer` and `EligibilityRule` are swappable. The interviewer will often change the ranking rule; that becomes a new class, not an edit to the service.
- **Repository**: storage behind interfaces; swap maps for JPA later without touching the service (Dependency Inversion).
- **Specification-style composition** for eligibility (`rule1.and(rule2)`).

## Code

```java
import java.util.*;
import java.util.stream.*;

record Skill(String name) {
    Skill { name = name.trim().toLowerCase(); }
}
record Candidate(String id, String name, Set<Skill> skills, int yoe) {}
record Job(String id, String recruiterId, String title, Set<Skill> required,
           int minYoe, int maxYoe, boolean open) {}
record Application(String candidateId, String jobId, long appliedAt) {}

interface EligibilityRule {
    boolean isEligible(Candidate c, Job j);
    default EligibilityRule and(EligibilityRule other) {
        return (c, j) -> isEligible(c, j) && other.isEligible(c, j);
    }
}

interface MatchScorer { double score(Candidate c, Job j); }

final class SkillAndExperienceScorer implements MatchScorer {
    public double score(Candidate c, Job j) {
        long matched = j.required().stream().filter(c.skills()::contains).count();
        double skillScore = j.required().isEmpty() ? 1.0 : (double) matched / j.required().size();
        int target = (j.minYoe() + j.maxYoe()) / 2;
        double yoeScore = 1.0 / (1 + Math.abs(c.yoe() - target));
        return 0.8 * skillScore + 0.2 * yoeScore;
    }
}

final class JobPortalService {
    private final Map<String, Job> jobs = new HashMap<>();
    private final Map<String, Candidate> candidates = new HashMap<>();
    private final Map<String, Set<String>> applicantsByJob = new HashMap<>();
    private final EligibilityRule eligibility;
    private final MatchScorer scorer;

    JobPortalService(EligibilityRule eligibility, MatchScorer scorer) {
        this.eligibility = eligibility; this.scorer = scorer;
    }

    void postJob(Job job) { jobs.put(job.id(), job); }
    void addCandidate(Candidate c) { candidates.put(c.id(), c); }

    void apply(String candidateId, String jobId) {
        Job job = require(jobs, jobId);
        Candidate c = require(candidates, candidateId);
        if (!job.open()) throw new IllegalStateException("job closed");
        if (!eligibility.isEligible(c, job)) throw new IllegalStateException("not eligible");
        boolean added = applicantsByJob.computeIfAbsent(jobId, k -> new HashSet<>()).add(candidateId);
        if (!added) throw new IllegalStateException("already applied");
    }

    List<Candidate> topCandidates(String jobId, int k) {
        Job job = require(jobs, jobId);
        Stream<Candidate> applicants = applicantsByJob.getOrDefault(jobId, Set.of())
                .stream().map(candidates::get);
        return topK(applicants, c -> scorer.score(c, job), k);
    }

    List<Job> topJobs(String candidateId, int k) {
        Candidate c = require(candidates, candidateId);
        Stream<Job> eligible = jobs.values().stream()
                .filter(Job::open)
                .filter(j -> eligibility.isEligible(c, j));
        return topK(eligible, j -> scorer.score(c, j), k);
    }

    /** Min-heap of size k: O(n log k). */
    private static <T> List<T> topK(Stream<T> items, java.util.function.ToDoubleFunction<T> score, int k) {
        record Scored<T>(T item, double score) {}
        PriorityQueue<Scored<T>> heap = new PriorityQueue<>(Comparator.comparingDouble(Scored::score));
        items.forEach(it -> {
            heap.offer(new Scored<>(it, score.applyAsDouble(it)));
            if (heap.size() > k) heap.poll();          // drop the worst
        });
        List<T> result = new ArrayList<>(heap.size());
        while (!heap.isEmpty()) result.add(heap.poll().item());
        Collections.reverse(result);                   // best first
        return result;
    }

    private static <V> V require(Map<String, V> map, String id) {
        V v = map.get(id);
        if (v == null) throw new NoSuchElementException(id);
        return v;
    }
}

class Demo {
    public static void main(String[] args) {
        EligibilityRule yoeInRange = (c, j) -> c.yoe() >= j.minYoe() && c.yoe() <= j.maxYoe();
        EligibilityRule hasASkill = (c, j) -> j.required().stream().anyMatch(c.skills()::contains);
        var portal = new JobPortalService(yoeInRange.and(hasASkill), new SkillAndExperienceScorer());

        portal.postJob(new Job("j1", "r1", "Backend Engineer",
                Set.of(new Skill("Java"), new Skill("Spring"), new Skill("SQL")), 3, 7, true));
        portal.addCandidate(new Candidate("c1", "Asha", Set.of(new Skill("java"), new Skill("sql")), 5));
        portal.addCandidate(new Candidate("c2", "Ravi", Set.of(new Skill("java")), 4));
        portal.apply("c1", "j1");
        portal.apply("c2", "j1");

        System.out.println(portal.topCandidates("j1", 1));   // Asha
        System.out.println(portal.topJobs("c2", 5));         // j1
    }
}
```

**Complexity**

- `topCandidates`: O(A × S + A log k), with A applicants and S required skills (skill check uses a `HashSet`).
- `topJobs`: O(J × S + J log k) over all open jobs J. Fine for an interview; see extensions for scale.

## Extensions

- **Many jobs:** keep an inverted index `Skill → Set<jobId>`. For `topJobs`, only score jobs that share at least one skill with the candidate, instead of scanning all J.
- **Precomputed rankings:** if reads dominate, keep a per-job sorted structure of applicants (`TreeSet` by score, ties by id) updated on apply. Must be recomputed if the scorer changes.
- **Weighted or must-have skills:** `Job` holds `Map<Skill, Weight>` and a set of mandatory skills; a new `MatchScorer` and `EligibilityRule`.
- **Concurrency:** make maps `ConcurrentHashMap` and the applicant set `ConcurrentHashMap.newKeySet()`. `add` returning false still enforces "apply once" atomically.
- **Application status:** Applied → Shortlisted → Interviewing → Offered / Rejected as an enum with allowed transitions.
- **Persistence:** with Postgres, a unique constraint on `(candidate_id, job_id)` enforces one application per job.

Related: [E1 · SOLID](../academy/lessons/E1.md), [E5 · The LLD interview method](../academy/lessons/E5.md).
