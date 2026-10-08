# Google: SWE III (L4) / Senior SWE (L5), Bengaluru & Hyderabad

Researched 2026-10-07. Everything here is `confidence: claimed`. The loop and the values come from
Google's own pages wherever Google publishes them. Actual questions come from candidate reports.
Question links point to the files the merge step will create under `data/questions/`.

## 1. TL;DR

- **2026 India L4 loop:** recruiter call, sometimes a Google Hiring Assessment, then **one coding
  screen and one Googleyness & Leadership (G&L) round**. Both are elimination rounds. After that,
  **two in-person coding onsites** in Bengaluru/Hyderabad, often on the same day
  ([8522064](https://leetcode.com/discuss/post/8522064/), [8551877](https://leetcode.com/discuss/post/8551877/),
  [8473279](https://leetcode.com/discuss/post/8473279/)). Then team match and hiring committee (HC).
  **L5:** 3 coding rounds, then system design (60 min) and G&L, which are scheduled only after
  2 of the 3 coding rounds come back Hire ([6668243](https://leetcode.com/discuss/post/6668243/)).
- **Coding is the gate at both levels.** The problems are rarely verbatim LeetCode. They are
  medium-hard problems wrapped in a vague story (routers, flights, movies, lakes, logs) with one or
  two follow-ups. Graphs (BFS, Dijkstra, union-find, topological sort) dominate, followed by
  intervals and line sweep, DP, and binary search on the answer.
- **Where people fail:** solving the problem but writing one big function ("code lacked
  readability", "didn't make proper use of classes") ([6846591](https://leetcode.com/discuss/post/6846591/),
  [8265899](https://leetcode.com/discuss/post/8265899/)). Also missing edge cases or not
  dry-running, running out of time on the follow-up, and, at L5, a system design they did not drive
  ([5946687](https://leetcode.com/discuss/post/5946687/)). One weak round can trigger an extra round
  or a down-level ([7164554](https://leetcode.com/discuss/post/7164554/), [6639707](https://leetcode.com/discuss/post/6639707/)).
- **Over-prepare:** graph modelling of story problems, union-find (including dynamic and offline
  variants), intervals and line sweep, streaming / time-window dedup (the logger family), and
  writing modular, tested code in a plain doc with no IDE. **For L5, over-prepare system design**
  of Google-flavoured products, with you driving.
- **Policy changes that matter:** every India SWE posting now says interviews are in person
  "in most instances"
  ([posting](https://www.google.com/about/careers/applications/jobs/results/101212497425375942-software-engineer-iii-full-stack-google-cloud)).
  Google bans AI tools in interviews ([how we hire](https://www.google.com/about/careers/applications/how-we-hire/)).
  Practise on paper or a whiteboard, not only in an editor.

## 2. The loop

**What Google says** ([how we hire](https://www.google.com/about/careers/applications/how-we-hire/),
[interview tips](https://www.google.com/about/careers/applications/interview-tips/)):

- The process takes roughly 6-8 weeks once interviews start. Google lists these steps:
  - Assessments: the *Google Hiring Assessment* (workstyle) and, where relevant, a coding exercise.
  - One or two recruiter conversations.
  - Project work, for some roles only.
  - A panel of interviews "over video or in person".
  - A combined decision, then the offer.
- Interviews are **structured**: the same rubrics for everyone and open-ended role-related
  questions, with no brainteasers.
- Google's re:Work guide names the attributes interviewers score: **problem solving,
  role-related knowledge and leadership**
  ([re:Work](https://rework.withgoogle.com/intl/en/guides/a-guide-to-structured-interviewing-for-better-hiring-practices)).

**What candidates report for L4 (India, 2025-26)**

| # | Round | Time | What it is |
|---|---|---|---|
| 0 | Recruiter screen | ~20 min | YOE (state it to the month), location, CTC. You can ask for 3-4 weeks to prepare, and a free mock interview ([6372760](https://leetcode.com/discuss/post/6372760/), [6512224](https://leetcode.com/discuss/post/6512224/)). |
| 0b | Google Hiring Assessment (some) | ~30 + ~60 min | Workstyle questionnaire, then a proctored 2-problem DSA OA (medium string + hard DP). Failing either means a 6-month cooldown ([8551877](https://leetcode.com/discuss/post/8551877/)). |
| 1 | Coding screen (TPS) | 45 min | One problem plus a follow-up in a shared doc with no IDE ([6522449](https://leetcode.com/discuss/post/6522449/)). Skipped if you interviewed recently. |
| 2 | G&L | 45 min | ~6-10 STAR questions. **In 2026 it sits in the screening stage** ([8522064](https://leetcode.com/discuss/post/8522064/), [8525084](https://leetcode.com/discuss/post/8525084/)). In 2024-25 it came after the onsites. |
| 3-4 | Onsite coding x2 (in person) | 45 min each | Medium-hard problem plus follow-ups. Interviewers note code quality ([8473279](https://leetcode.com/discuss/post/8473279/)). Pre-2026 and virtual loops had 3 coding rounds. |
| (5) | Extra coding round | 45 min | Added when one round is weak or HC wants more signal ([6735411](https://leetcode.com/discuss/post/6735411/), [7164554](https://leetcode.com/discuss/post/7164554/)). |
| 6 | Team match | 30 min per HM | Project deep dive and fit. Can take days to months ([7391662](https://leetcode.com/discuss/post/7391662/): "team matching hell for 2 months"). |
| 7 | HC, then offer | n/a | Reviews the packet. Can approve, ask for another round, or down-level. |

L4 candidates can sometimes swap one coding round for system design (2 DSA + 1 SD + G&L)
([6479658](https://leetcode.com/discuss/post/6479658/)).

**L5:** phone screen (sometimes skipped), 3 coding rounds, then **system design (60 min, sometimes
running to 80)** and G&L, then team match, HC and possibly references
([6668243](https://leetcode.com/discuss/post/6668243/), [5529760](https://leetcode.com/discuss/post/5529760/),
[6778854](https://leetcode.com/discuss/post/6778854/)). Some L5 loops add a "system integration"
round ([6552739](https://leetcode.com/discuss/post/6552739/)), or an extra system design round
after team match ([6349651](https://leetcode.com/discuss/post/6349651/)).

**Conflicts between Google's pages and reports:**

- Google says 6-8 weeks. Reports routinely say 3-6 months because of rescheduling, interviewer
  no-shows and team match.
- Google's pages don't name a "hiring committee"; every report describes one.
- Google says interviews are "over video or in person". Postings and 2026 reports show at least
  the onsites are in person in India.
- The Googleyness OA in 0b matches Google's own description of the *Google Hiring Assessment*.

## 3. The bar

**L4:** take a deliberately vague prompt and clarify it. Get to the optimal approach with at most
one hint and code it **cleanly** with helper functions and classes. Then test it yourself (dry run,
edge cases) and handle at least part of the follow-up, all in about 35 usable minutes.

- Recruiters told candidates that finishing the follow-up isn't mandatory at L4
  ([7093091](https://leetcode.com/discuss/post/7093091/)).
- Weak readability or missed edge cases sink packets even when the algorithm is right
  ([6846591](https://leetcode.com/discuss/post/6846591/)).
- Mixed packets (one Lean Hire) pass regularly ([6426991](https://leetcode.com/discuss/post/6426991/)).
  Two weak rounds usually don't.

**L5:** the same coding bar plus a **system design you drive**:

1. Requirements.
2. Back-of-envelope estimates.
3. High-level design.
4. Deep dives and bottlenecks.
5. Absorb a new requirement without re-architecting.

Coding follow-ups at L5 push on concurrency and scale: a thread-safe frame buffer, parallel
bisection, a CompletableFuture implementation ([5885342](https://leetcode.com/discuss/post/5885342/),
[6584641](https://leetcode.com/discuss/post/6584641/), [8323418](https://leetcode.com/discuss/post/8323418/)).

- A Lean Hire in system design ended one L5 loop ([5946687](https://leetcode.com/discuss/post/5946687/)).
- A Strong Hire in system design with mixed coding produced a **down-level to L4**
  ([6639707](https://leetcode.com/discuss/post/6639707/)).
- With 7+ years, Akhil will likely be calibrated for L5. If coding is the weaker leg, an L4 offer
  is the realistic fallback.

## 4. Question patterns

The question bank behind this section: 124 sightings of 114 unique questions in `data/_staging/google.json`.

Tag frequency across DSA sightings:

| Tag | Sightings |
|---|---|
| graph | 20 |
| string | 20 |
| hashmap | 17 |
| bfs | 14 |
| dynamic-programming | 12 |
| sorting, dfs, binary-search | 11 each |
| matrix | 10 |
| union-find, heap, greedy, design | 9 each |
| dijkstra | 7 |

What recurs, with question files:

- **Graph modelling from a story (the #1 pattern).**
  - Flights with timed connections: [earliest-arrival-with-flight-schedule](../../questions/dsa/earliest-arrival-with-flight-schedule.yaml) (2 sightings).
  - Routers that ping within a radius: [router-broadcast-minimum-time](../../questions/dsa/router-broadcast-minimum-time.yaml), [detonate-the-maximum-bombs](../../questions/dsa/detonate-the-maximum-bombs.yaml).
  - Alice/Bob meeting point or chase: [minimum-combined-travel-cost-meeting-point](../../questions/dsa/minimum-combined-travel-cost-meeting-point.yaml) (2), [shortest-path-escape-from-chaser](../../questions/dsa/shortest-path-escape-from-chaser.yaml).
  - Dependencies: [course-schedule-ii](../../questions/dsa/course-schedule-ii.yaml) (2), [build-a-matrix-with-conditions](../../questions/dsa/build-a-matrix-with-conditions.yaml).
  - Rooting a tree: [binary-tree-root-with-alternating-colors](../../questions/dsa/binary-tree-root-with-alternating-colors.yaml), **seen 3 times** (L4 2026, L5 2025 twice). Know the O(N) rerooting solution.
- **Union-find and connectivity over time.**
  - [the-earliest-moment-when-everyone-become-friends](../../questions/dsa/the-earliest-moment-when-everyone-become-friends.yaml), with an "unfriend" follow-up.
  - [last-day-where-you-can-still-cross](../../questions/dsa/last-day-where-you-can-still-cross.yaml).
  - [direct-vs-indirect-connections](../../questions/dsa/direct-vs-indirect-connections.yaml), [group-items-by-shared-attributes](../../questions/dsa/group-items-by-shared-attributes.yaml).
  - The 2026 roundup notes interviewers probe path compression and union-by-rank ([8546037](https://leetcode.com/discuss/post/8546037/)).
- **Grids:**
  - [count-lakes-in-island](../../questions/dsa/count-lakes-in-island.yaml), [number-of-distinct-islands](../../questions/dsa/number-of-distinct-islands.yaml), [number-of-islands](../../questions/dsa/number-of-islands.yaml).
  - [swim-in-rising-water](../../questions/dsa/swim-in-rising-water.yaml), [find-the-safest-path-in-a-grid](../../questions/dsa/find-the-safest-path-in-a-grid.yaml) (mouse and cat).
  - [universal-escape-sequence](../../questions/dsa/universal-escape-sequence.yaml).
- **Intervals and line sweep:**
  - [meeting-rooms-iii](../../questions/dsa/meeting-rooms-iii.yaml) (2), [worker-shift-schedule-intervals](../../questions/dsa/worker-shift-schedule-intervals.yaml).
  - [find-common-free-days](../../questions/dsa/find-common-free-days.yaml), [range-module](../../questions/dsa/range-module.yaml), [merge-intervals](../../questions/dsa/merge-intervals.yaml).
  - [maximum-profit-in-job-scheduling](../../questions/dsa/maximum-profit-in-job-scheduling.yaml).
- **Streams and time windows:**
  - [logger-rate-limiter](../../questions/dsa/logger-rate-limiter.yaml) (2, including the "suppress both duplicates" twist).
  - [moving-average-from-data-stream](../../questions/dsa/moving-average-from-data-stream.yaml) (2).
  - [top-k-chat-users-by-message-count](../../questions/dsa/top-k-chat-users-by-message-count.yaml), [design-stream-deduplicator](../../questions/lld/design-stream-deduplicator.yaml).
- **Binary search on the answer, and DP with an optimisation arc:**
  - [split-array-largest-sum](../../questions/dsa/split-array-largest-sum.yaml), [min-machines-for-earliest-completion](../../questions/dsa/min-machines-for-earliest-completion.yaml).
  - [maximum-retention-cap-per-category](../../questions/dsa/maximum-retention-cap-per-category.yaml), [binary-search-in-space-separated-string](../../questions/dsa/binary-search-in-space-separated-string.yaml).
  - [longest-increasing-subsequence-ii](../../questions/dsa/longest-increasing-subsequence-ii.yaml) (segment tree), [range-assignment-queries](../../questions/dsa/range-assignment-queries.yaml).
- **Parsing and recursion:**
  - [expand-template-variables-with-cycle-detection](../../questions/dsa/expand-template-variables-with-cycle-detection.yaml), **seen twice** (2025 and an in-person 2026 round).
  - [basic-calculator-ii](../../questions/dsa/basic-calculator-ii.yaml), [simplify-algebraic-expression](../../questions/dsa/simplify-algebraic-expression.yaml).
- **Cache design inside coding rounds:** [lfu-cache](../../questions/dsa/lfu-cache.yaml) (variant), [lru-cache](../../questions/dsa/lru-cache.yaml).

**HLD (L5):**

- [design-image-hosting-service](../../questions/hld/design-image-hosting-service.yaml) (2 sightings), [design-google-sheets](../../questions/hld/design-google-sheets.yaml), [design-distributed-cache](../../questions/hld/design-distributed-cache.yaml).
- [design-short-video-news-feed](../../questions/hld/design-short-video-news-feed.yaml), [design-social-feed-indexing-service](../../questions/hld/design-social-feed-indexing-service.yaml), [design-news-summary-app](../../questions/hld/design-news-summary-app.yaml).
- [design-inventory-management-system](../../questions/hld/design-inventory-management-system.yaml), [design-central-hr-system-integration](../../questions/hld/design-central-hr-system-integration.yaml).
- [design-employee-cab-service](../../questions/hld/design-employee-cab-service.yaml), [design-assistant-multi-device-dedup](../../questions/hld/design-assistant-multi-device-dedup.yaml), [design-event-aggregator-and-ticketing](../../questions/hld/design-event-aggregator-and-ticketing.yaml).

Two themes recur: **deduplication / entity resolution** (news, events, Assistant) and **Google
products** (Sheets, News, Assistant, an internal cab service).

**LLD (coding-round design):** [design-out-of-order-frame-reorder-buffer](../../questions/lld/design-out-of-order-frame-reorder-buffer.yaml),
[design-completable-future](../../questions/lld/design-completable-future.yaml),
[design-ad-server-with-cooldown](../../questions/lld/design-ad-server-with-cooldown.yaml),
[design-order-book-auction](../../questions/lld/design-order-book-auction.yaml).
Google has no separate LLD round. These appear as coding problems with a class-design flavour.

## 5. Behavioural

Google's own guidance ([interview tips](https://www.google.com/about/careers/applications/interview-tips/)):

- Use STAR.
- Give specific examples, not "I always do X".
- Quantify impact with "accomplished X as measured by Y by doing Z".
- Prepare a library of stories mapped to the job description.

The themes Google names map to stories as follows.

| Attribute (source) | Story Akhil should prepare | Reported questions |
|---|---|---|
| Navigating ambiguity (Google tips; "thrives in ambiguity", third-party) | A backend project that started with unclear or shifting requirements, where you made progress anyway: a spike, an ADR, an incremental rollout. | [handling-ambiguity](../../questions/behavioral/handling-ambiguity.yaml) |
| Working with people and teams, conflict (Google tips) | A technical disagreement with a peer or manager, resolved with data such as a benchmark, a design doc or a rollback plan. Also a cross-team dependency you unblocked. | [tell-me-about-a-conflict](../../questions/behavioral/tell-me-about-a-conflict.yaml), [disagree-with-majority-decision](../../questions/behavioral/disagree-with-majority-decision.yaml), [someone-took-credit-for-your-work](../../questions/behavioral/someone-took-credit-for-your-work.yaml) |
| Values feedback (third-party Googleyness) | Critical feedback you **gave**, specific and kind, plus the outcome. Negative feedback you **received** and the change you made. | [giving-critical-feedback](../../questions/behavioral/giving-critical-feedback.yaml), [receiving-negative-feedback](../../questions/behavioral/receiving-negative-feedback.yaml) |
| Leadership style, emergent leadership (Google tips, re:Work) | Leading without the title: mentoring a junior, turning around an underperformer, driving an incident retrospective or a migration plan. For L5, scope across 3-4 engineers. | [helping-an-underperforming-teammate](../../questions/behavioral/helping-an-underperforming-teammate.yaml) |
| Cares about the team (third-party) | Making someone feel included in a distributed or culturally diverse team, and handling an overloaded team. | [helping-someone-fit-in-diverse-team](../../questions/behavioral/helping-someone-fit-in-diverse-team.yaml) |
| Prioritisation and pressure | Several P0s at once: how you triaged, what you said no to, how you communicated it. A reasonable vs an unreasonable ask from a manager. | [everything-is-p0](../../questions/behavioral/everything-is-p0.yaml), [manager-unreasonable-demands](../../questions/behavioral/manager-unreasonable-demands.yaml) |
| Resilience, does the right thing | A failure or disappointment you owned: a production incident or a missed deadline, and the guardrail you added afterwards. | [biggest-disappointment](../../questions/behavioral/biggest-disappointment.yaml) |
| Puts the user first (third-party) | A change driven by user or customer feedback, such as an API ergonomics fix or a latency fix customers felt. | [why-google](../../questions/behavioral/why-google.yaml) (pair it with this story) |

Do not under-prepare G&L:

- In 2026 it is an **elimination round at the screening stage**.
- A "Lean Hire" on G&L, from stumbling on a diversity question, contributed to one down-level
  ([7164554](https://leetcode.com/discuss/post/7164554/)).
- A widely used question bank is [5963463](https://leetcode.com/discuss/post/5963463/). Several
  2025-26 candidates said most of their questions were on it.
- Prepare 7-8 stories that each flex to several questions ([8551877](https://leetcode.com/discuss/post/8551877/)).

## 6. Company-specific angle: scale, rigour and the packet

- **The packet is what gets judged, not the moment.** HC reads the interviewers' written notes
  ([5576037](https://leetcode.com/discuss/post/5576037/)). Make the signal easy to write down:
  - say your assumptions out loud;
  - name the complexity;
  - test without being asked;
  - structure code into small functions.
- **A Statement of Support from a hiring manager** during team match can break a tie at HC, and
  recruiters can escalate with HM backing ([5576037](https://leetcode.com/discuss/post/5576037/)).
- **Team match is where your backend background pays off.** HMs pick candidates who "have already
  solved such problems" ([6349651](https://leetcode.com/discuss/post/6349651/)).
  - Pitch Akhil's Java/Spring services as distributed-systems work: idempotency, retries,
    consistency, observability, scale numbers.
  - Expect several HM calls. Be honest but diplomatic if a team (for example Salesforce-heavy
    internal tooling) isn't a fit ([8473279](https://leetcode.com/discuss/post/8473279/)).
  - Team match can take weeks, so keep other loops warm.
- **Down-levelling is common.** L5 can drop to L4 and L4 to L3. Decide **before** interviewing
  whether you would take L4. The recruiter will ask, and L4 India total comp in 2025-26 reports is
  about ₹75-88L in year one versus about ₹1.2-1.5 Cr for L5 ([7520006](https://leetcode.com/discuss/post/7520006/),
  [6698788](https://leetcode.com/discuss/post/6698788/), [7432678](https://leetcode.com/discuss/post/7432678/),
  [5456292](https://leetcode.com/discuss/post/5456292/)).
- **Cooldowns:** about 6 months after a failed OA and about 12 months after failed interviews
  ([8551877](https://leetcode.com/discuss/post/8551877/)). Google's FAQ says "at least a year"
  before reapplying for the same type of role
  ([how we hire](https://www.google.com/about/careers/applications/how-we-hire/)).

## What the job postings ask for

These counts come from 11 India postings fetched on 2026-10-07: 5 SWE III and 6 Senior SWE,
across Cloud AI & Infra, Security/Zero Trust, Storage, Core, Core ML, Search and Corp Eng. Full
list in `company.yaml` under `official.job_postings`.

| Skill keyword | Postings (of 11) |
|---|---|
| data-structures-algorithms | 10 |
| design-and-code-reviews | 10 |
| debugging-triage | 10 |
| accessibility | 9 |
| software-design-architecture | 6 (every L5) |
| technical-leadership | 6 (every L5) |
| cpp | 5 |
| ml-infra | 4 |
| java / python / go | 3 each |
| distributed-systems, large-scale-infrastructure, cloud-gcp, full-stack, javascript-typescript, performance-optimization | 2 each |

What this means for prep beyond DSA:

- **DSA is listed in 10 of 11 postings**, which matches the interview weighting. It is the gate.
- **Every L5 posting asks for software design and architecture plus about a year of tech-lead
  scope** ("lead a small team of 3-4", "scope ambiguous problems"). The L5 G&L and system design
  rounds test this. Prepare stories where you set technical direction, not just delivered it.
- **Responsibilities are identical boilerplate:** write code, lead design reviews, review code
  against style/testability/efficiency, and triage production issues. Expect HMs to ask about your
  design-doc and code-review habits and on-call debugging stories.
- **The language mix leans C++** (5), with Java in 3. Java is accepted for interviews. For
  infrastructure teams (Search Serving Infra, Storage) C++ is preferred, so favour Cloud, Core and
  full-stack Cloud postings, which accept Java.
- **AI/ML infrastructure is the growth area:** 4 postings, plus "integrating GenAI/LLM tools" as a
  Search preference. A Java backend engineer is not expected to know ML, but being able to discuss
  serving, evaluation pipelines and data-processing backends helps in team match.
- **"Accessibility" appears in 9 of 11 postings** as a standard preference. It is low priority for
  interviews but worth a line in your resume if true.
- No posting mentions Spring. Translate Spring Boot experience into generic terms: service design,
  APIs, data stores, reliability.

## 6c. Tech stack and frameworks to prepare

Google interviews do not depend on your stack: the coding rounds use any mainstream language in a
plain doc editor. This section is for L5 system design depth and team-match conversations. Core and
common items only (full list with evidence in `company.yaml` → `tech_stack`). Sources: the 11 India
postings, [Software Engineering at Google](https://abseil.io/resources/swe-book), the
[SRE book](https://sre.google/sre-book/table-of-contents/) and Google's papers and docs.

| Group | Core | Common |
|---|---|---|
| **Frameworks & languages** | Production monitoring and triage (SRE) | C++ · Java · Python · Go · Protocol Buffers + gRPC · Spanner + Bigtable · Borg / Kubernetes · Google Cloud · testing · ML infrastructure |
| **Patterns** | Large-scale distributed system design · readable, testable, efficient code | none |
| **Tools** | Plain doc coding editor (interview) | Code review (Critique) and design docs |

**Revise** (Akhil already has these; refresh internals and trade-offs):

1. **Java for interview coding without an IDE** ([H1](../../academy/lessons/H1.md), [C1](../../academy/lessons/C1.md), [C3](../../academy/lessons/C3.md), [C4](../../academy/lessons/C4.md), [H2](../../academy/lessons/H2.md)): one medium a day in a plain text editor, stating complexity and testing by hand.
2. **Large-scale system design** ([F1](../../academy/lessons/F1.md), [F3](../../academy/lessons/F3.md), [F2](../../academy/lessons/F2.md), [F4](../../academy/lessons/F4.md), [F5](../../academy/lessons/F5.md), [F6](../../academy/lessons/F6.md)): one F5 case at Google scale with estimates stated first.
3. **Readable, testable code and review habits** ([E1](../../academy/lessons/E1.md), [H2](../../academy/lessons/H2.md)): skim the [Java style guide](https://google.github.io/styleguide/javaguide.html) and [reviewer guide](https://google.github.io/eng-practices/review/); write small named functions.
4. **Production debugging stories** ([H3](../../academy/lessons/H3.md)): two STAR stories, one incident and one design changed after review.

**Learn** (basic or new for Akhil, common here):

1. **Spanner, Bigtable and consistency** ([F2](../../academy/lessons/F2.md), [Q8](../../academy/lessons/Q8.md)): read the [TrueTime page](https://cloud.google.com/spanner/docs/true-time-external-consistency) and the [Bigtable paper](https://research.google/pubs/bigtable-a-distributed-storage-system-for-structured-data/) abstract; know when to choose each.
2. **SRE monitoring and SLOs** ([F6](../../academy/lessons/F6.md), [D8](../../academy/lessons/D8.md)): the [SLO](https://sre.google/sre-book/service-level-objectives/) and [monitoring](https://sre.google/sre-book/monitoring-distributed-systems/) chapters (four golden signals), used in every design answer.
3. **Protocol Buffers and gRPC** (no Academy module): define one `.proto`; explain field numbers, compatibility and streaming ([protobuf](https://protobuf.dev/overview/), [gRPC](https://grpc.io/docs/what-is-grpc/introduction/)).
4. **Borg / Kubernetes scheduling** (no Academy module): the [Borg paper](https://research.google/pubs/large-scale-cluster-management-at-google-with-borg/) abstract and the Kubernetes overview.
5. **Monorepo, Bazel and code review culture** (no Academy module): skim SWE book [ch. 9](https://abseil.io/resources/swe-book/html/ch09.html) and [ch. 16](https://abseil.io/resources/swe-book/html/ch16.html) for team-match vocabulary.

## 7. Strategy

Plan around about 25 minutes a day. Ordering of study items follows COMPANY-PREP.md §4.

**Last 4 weeks**

- One problem a day from §4, rotating graph modelling, union-find, intervals/line sweep, binary
  search on the answer, and streams/time windows.
- Write every solution in a **plain text doc**: no autocomplete, helper functions, a class when
  there is state, then a dry run. That is the in-person whiteboard/Doc reality.
- Twice a week, rewrite a past solution "Google-style": restate the vague prompt, list clarifying
  questions, then add the follow-up.
- L5: one system-design walkthrough per week from the HLD list. Drive it yourself in this order:
  requirements → estimates → API → data model → high-level design → deep dive → change request.
- Draft 7-8 STAR stories against the table in §5, each with a metric.

**Last week**

- Re-solve the repeat offenders without notes:
  - binary-tree-root-with-alternating-colors (O(N) rerooting)
  - logger-rate-limiter (bidirectional suppression)
  - expand-template-variables-with-cycle-detection
  - earliest-arrival-with-flight-schedule
  - the-earliest-moment-when-everyone-become-friends
  - meeting-rooms-iii
  - course-schedule-ii
- Say G&L answers out loud, aiming for under 2 minutes each.
- Ask the recruiter for the free mock interview if you haven't had it.
- Confirm the exact format: number of rounds, in person or not, and whether G&L is in the
  screening stage.

**Day before**

- Light review only: your notes on union-find, Dijkstra and topological-sort templates in Java
  (`PriorityQueue`, `ArrayDeque`, `TreeMap` APIs), plus your story list.
- Plan travel to the Bengaluru/Hyderabad office for in-person rounds.
- Remember AI tools are banned. Sleep.

## 8. Sources

Official (accessed 2026-10-07):

- [Our hiring process](https://www.google.com/about/careers/applications/how-we-hire/). JS-rendered; read in a browser.
- [Interviewing at Google: tips](https://www.google.com/about/careers/applications/interview-tips/)
- [Careers resources](https://www.google.com/about/careers/applications/buildyourfuture/resources/), where Tech Dev Guide now redirects.
- [re:Work structured interviewing guide](https://rework.withgoogle.com/intl/en/guides/a-guide-to-structured-interviewing-for-better-hiring-practices)
- 11 India job postings, listed in `company.yaml`.

News (2025): [Google reinstates in-person interviews](https://www.cxodigitalpulse.com/google-reinstates-in-person-job-interviews-amid-rising-ai-cheating-concerns/), reporting Pichai's town hall via CNBC.

Interview reports (LeetCode Discuss, read via LeetCode's public GraphQL because page fetches return 403):

| Date | Posts |
|---|---|
| 2026-10 | [8551877](https://leetcode.com/discuss/post/8551877/) |
| 2026-09 | [8525084](https://leetcode.com/discuss/post/8525084/), [8522064](https://leetcode.com/discuss/post/8522064/), [8525983](https://leetcode.com/discuss/post/8525983/) (compilation), [8546037](https://leetcode.com/discuss/post/8546037/) (roundup) |
| 2026-08 | [8473279](https://leetcode.com/discuss/post/8473279/) |
| 2026-06 | [8355549](https://leetcode.com/discuss/post/8355549/), [8323418](https://leetcode.com/discuss/post/8323418/) |
| 2026-05 | [8265899](https://leetcode.com/discuss/post/8265899/), [8218498](https://leetcode.com/discuss/post/8218498/) |
| 2026-03 | [7631077](https://leetcode.com/discuss/post/7631077/), [7624444](https://leetcode.com/discuss/post/7624444/) |
| 2026-01 | [7469970](https://leetcode.com/discuss/post/7469970/), [7520006](https://leetcode.com/discuss/post/7520006/) |
| 2025-12 | [7432678](https://leetcode.com/discuss/post/7432678/), [7391662](https://leetcode.com/discuss/post/7391662/), [7390147](https://leetcode.com/discuss/post/7390147/) |
| 2025-09 | [7164554](https://leetcode.com/discuss/post/7164554/) |
| 2025-08 | [7093123](https://leetcode.com/discuss/post/7093123/), [7093091](https://leetcode.com/discuss/post/7093091/) |
| 2025-07 | [7020053](https://leetcode.com/discuss/post/7020053/) |
| 2025-06 | [6846591](https://leetcode.com/discuss/post/6846591/) |
| 2025-05 | [6796476](https://leetcode.com/discuss/post/6796476/), [6782644](https://leetcode.com/discuss/post/6782644/), [6778854](https://leetcode.com/discuss/post/6778854/), [6775581](https://leetcode.com/discuss/post/6775581/), [6743327](https://leetcode.com/discuss/post/6743327/), [6735411](https://leetcode.com/discuss/post/6735411/) |
| 2025-04 | [6668243](https://leetcode.com/discuss/post/6668243/), [6639707](https://leetcode.com/discuss/post/6639707/), [6634286](https://leetcode.com/discuss/post/6634286/), [6698788](https://leetcode.com/discuss/post/6698788/) |
| 2025-03 | [6596001](https://leetcode.com/discuss/post/6596001/), [6584641](https://leetcode.com/discuss/post/6584641/), [6552739](https://leetcode.com/discuss/post/6552739/), [6522571](https://leetcode.com/discuss/post/6522571/), [6522449](https://leetcode.com/discuss/post/6522449/), [6512224](https://leetcode.com/discuss/post/6512224/), [6492044](https://leetcode.com/discuss/post/6492044/), [6479658](https://leetcode.com/discuss/post/6479658/), [6479105](https://leetcode.com/discuss/post/6479105/) |
| 2025-02 | [6426991](https://leetcode.com/discuss/post/6426991/), [6372760](https://leetcode.com/discuss/post/6372760/) |
| 2025-01 | [6349651](https://leetcode.com/discuss/post/6349651/) |
| 2024 | [5963463](https://leetcode.com/discuss/post/5963463/), [5946687](https://leetcode.com/discuss/post/5946687/), [5898854](https://leetcode.com/discuss/post/5898854/), [5885342](https://leetcode.com/discuss/post/5885342/), [5576037](https://leetcode.com/discuss/post/5576037/), [5534774](https://leetcode.com/discuss/post/5534774/), [5529760](https://leetcode.com/discuss/post/5529760/), [5456292](https://leetcode.com/discuss/post/5456292/), [5285771](https://leetcode.com/discuss/post/5285771/) |

Aggregate list: [liquidslr/leetcode-company-wise-problems, Google, last 3 months](https://github.com/liquidslr/leetcode-company-wise-problems/blob/main/Google/2.%20Three%20Months.csv), updated 2026-08-16.

Not fetched or blocked:

- Glassdoor: search snippets only.
- igotanoffer: 403.
- Blind: not attempted.
- Third-party Googleyness attribute lists are from search snippets (educative, igotanoffer).
