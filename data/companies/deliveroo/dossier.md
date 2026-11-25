# Deliveroo — interview dossier

Target: **Software Engineer II, backend, Hyderabad** (referral, no interview date yet). Researched 24-25 Nov 2026.
Everything from candidate reports is `claimed`. Much of the Glassdoor text was read from search snippets, so wording can be partial.

## 1. TL;DR

- **Check the board first.** On 24 Nov 2026 Deliveroo's public board had no open SE II in Hyderabad (only Senior, Staff and EM). The nearest open match was DoorDash India "Software Engineer, Backend", Hyderabad (3+ years, posted 23 Nov).
- **Ask which process applies, and whether AI is allowed.** Deliveroo: no AI in live rounds. DoorDash (Mar 2026): a 60-minute coding session where AI use is expected.
- **The loop:** recruiter call, 90-minute HackerRank, then coding, architecture and behavioural, 60 minutes each.
- **Three things repeat everywhere:** the cron-parser take-home with live extensions, the 6-million-burger design, and the HackerRank pair (rate limiter plus paginated REST API).
- **Where people fail (reported):** missing tests and docs in the take-home, unseen coding (authentication manager), and the behavioural round on its own.

## 2. The loop

| Round | What it assesses | Time |
|---|---|---|
| Recruiter prescreen | Background, interest, logistics | 20-45 min |
| HackerRank | 15 MCQs (20 min), then algorithms and REST API coding (70 min); no going back | 90 min |
| Coding | Take-home walkthrough plus live extension, **or** a live HackerRank problem; no AI | 60 min |
| Architecture | Product brief to a design you could hand to a team; no AI | 60 min |
| Behavioural | STAR stories | 60 min |
| Committee, then team match | No interview | - |

- Source for the official steps: [engineers' guide](https://careers.deliveroo.co.uk/interview-guide-for-engineers/) and [backend guide](https://careers.deliveroo.co.uk/our-interview-guide-backend-engineering/) (Feb-Mar 2026).
- The guide gives no split by level or country. Which coding option Hyderabad SE II gets is **not published**; prepare both.
- **Older reports:** Karat as the first step (2022-23), and campus rounds with three DSA problems plus SQL and system design (2024).

## 3. The bar

- No SE II bar is published for India.
- The London SE II posting (Oct 2026) names team goals, monitoring and system health, code review, influence on the roadmap and interviewing.
- Inference: tested, readable, extensible code and a design with failure modes beat clever algorithms.

## 4. Question patterns

- **Take-home and extensions:** [cron parser](#/q/cron-expression-parser-take-home). 75 public candidate repos were found (2018 to Nov 2025); 5 are kept as sightings.
- **HackerRank:** [MCQ topics](#/q/hackerrank-backend-mcq), [rate limiter](#/q/design-rate-limiter), [logger rate limiter](#/q/logger-rate-limiter), [paginated REST API](#/q/paginated-rest-api-top-result).
- **Unseen coding:** [authentication manager](#/q/design-authentication-manager) replaced the take-home for one Nov 2025 candidate and tops the LeetCode tag lists.
- **Design:** [6M burgers](#/q/design-flash-giveaway-free-burgers), then [sudden load](#/q/design-system-for-sudden-load), [vouchers](#/q/design-voucher-application-service), [food delivery](#/q/design-food-delivery-app).
- **Campus DSA (2024):** vertical traversal, invalid parentheses, smallest range, nice subarrays.
- **Not reported:** Java or Spring theory. SQL appears only as a follow-up or in campus rounds.

## 5. Behavioural

Published examples: a [missed deadline](#/q/missed-a-deadline), a [challenge you saw through](#/q/most-challenging-project), a [difference of opinion](#/q/tell-me-about-a-conflict). Also reported: [mentoring juniors](#/q/guiding-junior-developers), [failure](#/q/tell-me-about-a-failure), [cross-functional decisions](#/q/driving-cross-functional-decisions), [impact](#/q/business-impact-you-created).

| What they listen for | Story to choose from the story bank |
|---|---|
| Customer first, operational excellence | A production issue you owned end to end |
| Bias to action, ownership | Something you fixed that was nobody's job |
| Seek disconfirming evidence, best idea wins | A decision where data changed your mind |
| Company > team > self | Help you gave another team |
| Embrace new technology | The LLM or automation work |

## 6. Company-specific angle

- **Language:** interview in Java (the process is language agnostic), and have an honest answer to "are you happy to move to Go?".
- **Scale words:** postings mention 20k+ requests per minute; Kafka, DynamoDB and Redis are core.
- **Merger:** Deliveroo, DoorDash and Wolt are moving to one stack, so a good question is which parts of the Hyderabad roadmap stay on Go and Ruby.

## 6b. What the job postings ask for

- 4 of 4 Hyderabad texts: Go/Ruby/Python/Scala/Rust, PostgreSQL, Redis, DynamoDB, Elasticsearch, scale, testing. **Java 0 of 4.**
- The 2024-25 postings accepted "any language" plus interest in Go. The Oct 2026 Senior posting drops that; the Staff one keeps "a strong desire to learn".

## 6c. Tech stack and frameworks to prepare

| Group | Core | Common |
|---|---|---|
| Frameworks & languages | Go, Ruby/Rails, REST | Python, Scala, Rust |
| Patterns | Service-oriented, event-driven over Kafka | Stream aggregation (mentioned) |
| Tools | AWS, ECS, PostgreSQL, DynamoDB, Redis, Kafka | Elasticsearch, Snowflake, SQS, Terraform, Datadog |

- **Revise:** REST and HTTP clients (REST lessons), PostgreSQL, testing, resilience and system design.
- **Learn:** Go basics, DynamoDB, Kafka, Redis, AWS/ECS.

## 6d. If it turns out to be DoorDash's process

- DoorDash (Mar 2026): one 60-minute AI-assisted session replaces traditional coding; agent mode allowed, starter code in your language. Whether India uses it is unknown.
- Reported recurring rounds, held in `data/_staging/held/doordash.json` (not merged): food review and voting system design, API aggregator, refund workflow DAG, Dasher payout, and debugging broken services.
- Practice then: one timed session with your AI tool where you state the plan, verify every change and narrate trade-offs.

## 7. Strategy

- **Now:** ask the referrer for the job link or ID, the board, and the process.
- **Before HackerRank:** MCQ sheet, rate limiter and paginated API from a blank editor, one timed 90-minute mock.
- **Before the loop:** your own cron parser (write it yourself, AI-written submissions are rejected), two extension drills, two blank-page designs, six behavioural stories said aloud.
- **Last day:** re-read your own code and README, redraw the burger design, sleep.

## 8. Sources

Harvest log (24 Nov 2026): official guides, careers site, the Greenhouse API for both boards, SEC filings and the engineering blog; Glassdoor and mirrors, Blind, interviewexperiences.in, GeeksforGeeks, LeetCode posts, the LeetCode company tag list, FleetCode and GitHub.

- **Nothing found:** AmbitionBox, Naukri, Indeed, Levels.fyi, Reddit (blocked), Quora, YouTube.
- **Partial:** Glassdoor, Blind and LeetCode pages refused fetches, so wording came from search snippets and about a third of the job-board months are estimates (marked in the sighting's round).
- **Dropped:** 11 items too vague or off-stack (iOS, ML, analytics, unnamed problems) and 70 of 75 GitHub repos.
- **No 2026 Hyderabad SE II interview report was found.**
