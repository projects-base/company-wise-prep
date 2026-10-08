# Entain India — interview dossier

Target: Software Development Engineer II, Entain India (Hyderabad). Round 2 is on **Tuesday
13 October 2026, 3:00 PM IST, on Microsoft Teams**. Candidate: about 7 years of Java/Spring Boot
backend. Researched 2026-10-08.

**Confidence.** Two things are `verified` because Akhil saw them himself: the HR email of
2026-10-08, which names the round's focus areas, and the 18 Sep round-1 invite topics. Everything
else is `claimed`.

**Source quality.** Thin. Entain's careers sites have no hiring-process or interview-tips page
(checked the sitemap). The job search needs JavaScript, so postings came from the SmartRecruiters
public API (1 live India engineering posting) and 9 aggregator copies of recently closed Entain
India postings. Glassdoor and AmbitionBox return 403, so Glassdoor reports were read only through
search-engine snippets. LeetCode Discuss (GraphQL) has a single Ivy Comptech interview write-up
(Sep 2019). No Entain-specific report of a betting-platform design question was found anywhere.
The bank therefore holds 13 new Entain questions, and the round-specific prep below relies on
topic areas more than on reported questions.

## 1. TL;DR

- **The round:** one technical conversation on **System Design + Database performance and
  scalability + SQL** (HR email, verified). Expect a design problem with a real data model, then
  database deep dives (indexes, EXPLAIN, transactions and locking, read and write scaling), and
  one or more SQL queries you write live. The invite names the recruiter, Boddu V V Rama Pavan
  Vamsi Krishna Kumar, who most likely only organises the call. An engineer will probably run the
  technical hour.
- **Over-prepare the database half.** Every one of the 10 India postings lists SQL. The backend
  SDE II/III postings add "SQL, indexing, migrations". SQL also turns up in Entain India reports
  from 2019, 2021, 2023 and Apr 2026 ("SQL queries and optimization"). Know top-N per group
  (`DENSE_RANK`), the second/Nth highest salary, running totals, and gaps and islands. Be able to
  explain a slow query from its EXPLAIN plan.
- **Use the domain as your design default.** If you get to pick, or the interviewer says "design
  something you know", use a sportsbook flow: bet placement → wallet debit → settlement, with
  live in-play odds spikes. The insider tip (claimed) names event-driven systems, WebSockets, SQL,
  AWS, multithreading and casino/betting architecture.
- **Their real stack is Postgres on AWS.** Entain runs EKS and moved its databases to **Aurora
  PostgreSQL** with DMS at near-zero downtime
  ([AWS case study](https://aws.amazon.com/solutions/case-studies/entain-case-study/)). Answer in
  Postgres terms: MVCC, `EXPLAIN ANALYZE`, B-tree/partial/covering indexes, Aurora read replicas,
  partitioning, and connection pooling (PgBouncer/RDS Proxy).
- **Where candidates lose points (inferred):** a design with no data model; "add an index" with
  no reason given; no answer for double-spend or idempotent bet placement; SQL that breaks on
  ties or NULLs.

## 2. The loop

**What Akhil has seen (verified):**

| # | Round | Date | Topics |
|---|---|---|---|
| 1 | Technical (Teams, ~1 h) | 18 Sep 2026 | Java Concepts · Coding Concepts · Problem Solving. **Passed.** |
| 2 | Technical (Teams) | **13 Oct 2026, 3:00 PM IST** | **System Design · Database performance and scalability · SQL** (HR email, 2026-10-08) |

**What Entain says:** nothing role-specific. The careers sites have no "how we hire" page for
tech roles ([sitemap](https://careers.entainindia.com/sitemap/)). Postings invite requests for
reasonable adjustments at any stage. The Hyderabad Product & Tech recruiter's published advice:
"understand why Entain builds scalable tech", focus on solving problems at speed, and show how you
think, adapt and **design for scale**
([Apr 2025](https://wherewomenwork.com/Career/6660/Entain-careers-recruitment-team-job-interview-advice)).

**What candidates report (Glassdoor, claimed):**

- **SDE 2, Hyderabad, Jul 2024:** a HackerRank test with 2 questions; a technical round with 3
  coding problems (LRU cache, longest consecutive sequence) plus "some design" and sync vs async
  calls in Spring Boot; a techno-managerial round; an HR competency round. The whole process took
  3 days ([SDE 2 page](https://www.glassdoor.co.in/Interview/Entain-India-SDE-2-Interview-Questions-EI_IE257530.0,12_KO13,18.htm)).
- **SDE 1, Hyderabad, Apr 2024:** a 1-hour coding round (find peak element); a technical round on
  core Java, system design basics, Kafka architecture, Redis, Spring Boot and SQL; a managerial
  round ([Hyderabad page](https://www.glassdoor.com/Interview/Entain-Hyderabad-Interview-Questions-EI_IE341832.0,6_IL.7,16_IM1076.htm)).
- **Principal Engineer, Hyderabad, Aug 2025:** OA, hiring-manager screen, then a design-and-coding
  round: design a distributed queue with multiple producers and consumers (same page).
- **Software Engineer, Apr 2026:** Java concepts (OOP, collections, arrays, linked lists), coding,
  and **SQL queries and optimization**. The candidate rated it easy and accepted
  ([Entain India page](https://www.glassdoor.co.in/Interview/Entain-India-Interview-Questions-E257530.htm)).
- **Senior SE, Feb 2023:** JVM internals and **SQL query optimization**
  ([Senior SE page](https://www.glassdoor.com/Interview/Entain-India-Senior-Software-Engineer-Interview-Questions-EI_IE257530.0,12_KO13,37.htm)).

**How this compares with Akhil's process:** the reports put coding first and design in a later
round. Akhil's split, Java/coding first and then a dedicated design + database + SQL round,
matches that shape. No report shows a round devoted to databases the way Akhil's round 2 is, so
treat the HR email as the only authority on content.

## 3. The bar

Postings define SDE II as someone who designs and delivers "well-defined, moderately complex
features", debugs across dev, test and prod, is the **initial on-call responder**, drives the
**RFC process** and mentors juniors
([SDE II Hyderabad, Jul 2026](https://www.talentmate.com/jobs/india/hyderabad/software-development-engineer-ii/2607-11073-77)).
For this round, a hire probably looks like this (inference, not stated by Entain):

- You clarify requirements and estimate load (bets/sec at peak, read:write ratio) before drawing
  anything.
- The data model comes early: tables, keys, the indexes each query needs, and what must be
  transactional.
- Scaling choices have reasons: replicas for reads, partitioning by time or event, cache for odds,
  a queue for settlement. Each one comes with its cost (replica lag, hot partitions, cache
  invalidation).
- The SQL is correct the first time, including ties, NULLs and empty results, and you can say what
  plan the database will choose.
- You give production answers: how you would find a slow query (`pg_stat_statements`, slow-query
  log, `EXPLAIN (ANALYZE, BUFFERS)`), how you would roll out an index without locking
  (`CREATE INDEX CONCURRENTLY`), and how a migration runs safely (expand → migrate → contract).

## 4. Question patterns

Staged in `data/_staging/entain.json` (now merged): 18 sightings, 13 new questions, 3 merged into
existing ones (`lru-cache`, `longest-consecutive-sequence`, `search-in-rotated-sorted-array`).

| Type | Questions (with Entain sightings) |
|---|---|
| **SQL** (new type) | [`sql/second-highest-salary`](../../questions/sql/second-highest-salary.yaml) (2019 SSE, 2021 Test Eng), [`sql/sql-query-optimization`](../../questions/sql/sql-query-optimization.yaml) (2023 SSE, **Apr 2026 SE**), [`sql/sql-join-types`](../../questions/sql/sql-join-types.yaml) (2021) |
| HLD | [`hld/design-distributed-message-queue`](../../questions/hld/design-distributed-message-queue.yaml) (Aug 2025) |
| DOMAIN | [`domain/kafka-architecture`](../../questions/domain/kafka-architecture.yaml), [`domain/redis-internals`](../../questions/domain/redis-internals.yaml) (Apr 2024) |
| DSA | [`dsa/lru-cache`](../../questions/dsa/lru-cache.yaml), [`dsa/longest-consecutive-sequence`](../../questions/dsa/longest-consecutive-sequence.yaml) (Jul 2024), [`dsa/find-peak-element`](../../questions/dsa/find-peak-element.yaml) (Apr 2024), [`dsa/search-in-rotated-sorted-array`](../../questions/dsa/search-in-rotated-sorted-array.yaml), [`dsa/wiggle-sort`](../../questions/dsa/wiggle-sort.yaml) (2019) |
| JAVA | [`java/jvm-internals`](../../questions/java/jvm-internals.yaml) (2023), [`java/java-create-and-fix-deadlock`](../../questions/java/java-create-and-fix-deadlock.yaml), [`java/review-send-mail-notification-method`](../../questions/java/review-send-mail-notification-method.yaml), [`java/store-many-to-one-pairs`](../../questions/java/store-many-to-one-pairs.yaml) (2019) |
| SPRING | [`spring/spring-sync-vs-async-calls`](../../questions/spring/spring-sync-vs-async-calls.yaml) (Jul 2024) |

**Relevant to 13 Oct but from other companies' sightings**, already in the bank:
`hld/design-payment-system`, `hld/idempotency-in-payment-systems`, `hld/design-idempotent-api`,
`hld/rdbms-scaling-vs-nosql`, `domain/db-indexing-partitioning-sharding`,
`domain/dbms-replica-snapshot-checkpoint`, `spring/jdbc-connection-pooling`,
`lld/design-leaderboard`, `hld/design-distributed-cache`, `hld/design-ticket-booking`
(inventory under concurrency, the same shape as a liability limit),
`hld/design-chat-application` (WebSockets fan-out), `domain/cap-theorem`.

**SQL practice set.** These are practice choices, not reported Entain questions. LeetCode
Database: `second-highest-salary` (176), `nth-highest-salary` (177), `rank-scores` (178),
`consecutive-numbers` (180), `department-highest-salary` (184), `department-top-three-salaries`
(185), `rising-temperature` (197), `trips-and-users` (262), `game-play-analysis-iv` (550),
`human-traffic-of-stadium` (601), `last-person-to-fit-in-the-bus` (1204), `restaurant-growth`
(1321).

**Database topics to expect** (from the HR focus areas and the postings' "SQL, indexing,
migrations"; the list is an inference):

1. **Indexes:** B-tree internals; composite index column order (equality columns first, then
   range); covering indexes (`INCLUDE`); partial indexes (`WHERE status = 'OPEN'`); why a
   low-selectivity index is ignored; the write cost of every index; and functions that defeat an
   index (`WHERE date(created_at) = ...`).
2. **Reading EXPLAIN:** seq vs index vs bitmap scan, nested loop vs hash vs merge join, row
   estimates vs actuals, stale statistics (`ANALYZE`).
3. **Transactions and concurrency:** ACID; isolation levels and the anomalies each one allows;
   Postgres MVCC (snapshots, dead tuples, VACUUM); row locks with `SELECT ... FOR UPDATE` vs
   optimistic versioning; deadlocks and lock ordering. Apply them to wallet debits with no
   double-spend.
4. **Query performance in the app:** N+1 in JPA (fetch joins, `@EntityGraph`, batch size);
   keyset vs OFFSET pagination; batch inserts; connection-pool sizing (Hikari) and why more
   connections can be slower.
5. **Scaling reads:** read replicas (Aurora up to 15) and replica lag; read-your-writes; caching
   (Redis cache-aside, TTL, stampede); materialised views or CQRS read models for odds and
   bet history.
6. **Scaling writes and data size:** partitioning (range by date for bets and transactions);
   sharding by customer ID; hot keys (one big match); archiving settled bets; append-only ledgers.
7. **Schema changes in production:** online migrations (expand/contract), `CREATE INDEX
   CONCURRENTLY`, Flyway/Liquibase, backfills in batches.
8. **SQL vs NoSQL:** when a wallet must be relational (ACID ledger), and when odds or sessions fit
   a KV store or cache.

## 5. Behavioural

Entain's values ([mission & values](https://entaincareers.com/our-offer/mission-values/)):
**Do what's right · Keep it simple · Go beyond · Win together.** A 2026 Entain India posting
lists "Maker Dimensions": AI fluency, agency and accountability, systems thinking, stakeholder
engagement, improving performance, cross-functional versatility, product sense, building
capability ([Portfolio Lead](https://jobs.smartrecruiters.com/Entain/744000154403050-portfolio-lead)).

This round is technical, but design rounds often open with "a system you built". Map stories:

- **Do what's right / player protection:** a time you put data integrity or correctness above
  speed. Example: refusing to ship a payment or ledger change without idempotency.
- **Keep it simple:** a time you removed a component or chose Postgres over a new datastore.
- **Go beyond / systems thinking:** a production performance incident you diagnosed (slow query,
  pool exhaustion, lock contention) and the fix that held.
- **Win together / agency:** an RFC or design doc you drove across teams; on-call ownership.
- **AI fluency:** recent postings name Kiro and Copilot. Have one sentence on how you use AI tools
  and verify their output.

## 6. Company-specific angle: real-time betting on Postgres

**The betting domain in brief** (background for design answers; this is general domain knowledge,
not a description of Entain's internals):

- **Sportsbook:** events (matches) → markets (match result, total goals) → selections (home, draw,
  away), each with **odds**. Odds come from trading/pricing feeds and change constantly, most of
  all **in-play**, during the match. Markets get **suspended** around goals and red cards.
- **Bet placement:** the customer picks selections (single or accumulator) and a stake. The
  server re-validates the price (**price-change** handling: accept or reject if the odds moved),
  checks market status, **limits and liability**, and responsible-gambling rules. It then
  **debits the wallet** and records the bet. This must be **idempotent** (client request ID),
  because retries on a flaky mobile network are normal.
- **Wallet:** a ledger of balance-affecting transactions (deposit, stake, win, refund, bonus).
  Model it as an append-only ledger plus a balance row that is updated in the same transaction
  under a row lock or version check. Never update the balance without a matching ledger row.
- **Settlement:** when the result is known, a settlement job grades every open bet on the market
  and credits winnings. It is a batch fan-out (one goal can settle millions of bets). It must be
  idempotent and resumable, because results get corrected (resettlement).
- **Responsible gambling:** deposit and loss limits, time limits, self-exclusion, cool-off,
  affordability checks. These are synchronous checks on the placement and deposit paths. Entain's
  mission is "market-leading player protection"
  ([values](https://entaincareers.com/our-offer/mission-values/)).
- **Traffic shape:** big spikes at kick-off and on in-play events. Odds updates fan out to many
  clients, usually over **WebSockets/SSE** (one SDE II posting lists WebSockets/SSE; the insider
  tip names WebSockets). Reads far outnumber bet writes, and writes cluster on the same few
  markets.

**How to use this in the round:**

- **Default design ("design a betting platform / bet placement"):** API gateway → bet service
  (stateless, on EKS) → wallet service (Aurora Postgres, ledger + balance) → Kafka events
  (`BetPlaced`, `MarketSettled`) → settlement workers → notification/WebSocket gateway. Serve odds
  from a cache (Redis) fed by the pricing stream, with a short TTL and a version per selection. Run
  bet history reads from a replica or read model.
- **Database deep dives to volunteer:**
  - The wallet debit: one transaction with `SELECT ... FOR UPDATE` on the balance row, or
    optimistic `UPDATE ... WHERE version = ?`.
  - A unique constraint on `(customer_id, request_id)` for idempotency.
  - An outbox table so the Kafka event and the DB write cannot diverge.
  - Partition `bets` by placed date; a partial index on open bets per market for settlement.
  - Keyset pagination for bet history.
- **Hot spots:** one popular match means one hot market. For liability counters, shard the
  counter or aggregate in memory and reconcile. Put back-pressure on placement during
  suspensions.

**Entain's actual stack signals (cite these if asked "how would you do it at Entain"):**

- EKS as the base of the internal developer platform; databases on Aurora PostgreSQL, migrated with
  DMS at near-zero downtime; deploy time cut from weeks to 2 hours; Kiro for modernisation
  ([AWS](https://aws.amazon.com/solutions/case-studies/entain-case-study/)).
- Streaming on Redpanda (Kafka-compatible) into Snowflake, used for live odds, fraud detection
  and player safety. This is a vendor claim
  ([Redpanda via The Register](https://whitepapers.theregister.com/paper/view/38707/simplifying-real-time-data-streaming-for-analytics-at-scale)).

## 6b. What the job postings ask for

10 India postings: 1 live on SmartRecruiters (SDE III, 2026-10-07) and 9 aggregator copies from
May 2025 to Sep 2026 (SDE I/II/III Java, SDE II full-stack Pune, Principal SE CRM, Junior Data
Engineer). The text is nearly identical across the SDE levels.

| Skill | Postings | What it implies for prep |
|---|---|---|
| sql | 10 | Live SQL writing; index and plan reasoning |
| ci-cd, testing | 10, 9 | Talk about how a schema change ships safely |
| java | 9 | Your strength; say how JPA and Hikari behave under load |
| docker-kubernetes | 9 | EKS in your design; stateless services, HPA |
| distributed-systems, microservices | 8, 8 | Service boundaries in the design |
| observability, on-call | 8, 8 | How you would find a slow query in production |
| event-driven | 7 | Kafka, outbox pattern, idempotent consumers |
| rfc-process, mentoring | 8, 9 | Mention design docs / RFCs you wrote |
| sql-indexing-migrations | 3 (all backend SDE II/III) | Indexes + zero-downtime migrations |
| kafka, redis, websockets, aws | 3, 1, 1, 2 | Matches the insider tip |
| ai-assisted-dev (Kiro, Copilot) | 3 | One sentence ready |

Posting bullets are requirements, not interview questions.

## 6c. Tech stack and frameworks to prepare

Core and common items only (full list with evidence in `company.yaml` → `tech_stack`). Sources: the
India postings, the [AWS case study](https://aws.amazon.com/solutions/case-studies/entain-case-study/)
and the [Redpanda vendor case study](https://whitepapers.theregister.com/paper/view/38707/simplifying-real-time-data-streaming-for-analytics-at-scale).
Entain's GitHub orgs have no public repos, and no engineering blog or conference talks were found.

| Group | Core | Common |
|---|---|---|
| **Frameworks & languages** | Java · SQL · PostgreSQL on Aurora · Docker · Kubernetes (EKS) · CI/CD · monitoring + logging · testing · secure coding | AWS (EKS, Aurora, DMS) · Spring Boot · Spring Data JPA/Hibernate · Kafka (Redpanda) · Angular/JS (full-stack roles only) · WebSockets/SSE · AI tools (Kiro, Copilot) |
| **Patterns** | Microservices · event-driven architecture · distributed architectures · database scaling (indexing, replicas, partitioning) · OO design + design patterns | API integration / API-first · zero-downtime database migration |
| **Tools** | Git | Swagger / OpenAPI |

The order below follows the 13 Oct round's focus: SQL, then database performance and scalability,
then system design.

**Revise** (Akhil already has these; refresh internals and trade-offs):

1. **SQL query writing** ([Q1](../../academy/lessons/Q1.md)–[Q4](../../academy/lessons/Q4.md)): write top-N-per-group, running-total and gaps-and-islands queries on a bets/wallet schema, out loud.
2. **PostgreSQL indexes and EXPLAIN** ([Q5](../../academy/lessons/Q5.md)): B-tree, composite, partial and covering indexes on `bets`; read one EXPLAIN ANALYZE before and after.
3. **Transactions, isolation, locking, MVCC** ([Q6](../../academy/lessons/Q6.md)): wallet debit with `FOR UPDATE` vs a version column; the anomaly each isolation level allows.
4. **Query performance in practice** ([Q7](../../academy/lessons/Q7.md), [D6](../../academy/lessons/D6.md), [L7](../../academy/lessons/L7.md)): an N+1 or pool-exhaustion story; keyset pagination for bet history.
5. **System design and distributed architectures** ([F1](../../academy/lessons/F1.md), [F3](../../academy/lessons/F3.md), [F2](../../academy/lessons/F2.md), [F8](../../academy/lessons/F8.md)): run F8 (sports betting platform) end to end in 45 minutes.
6. **Microservices patterns** ([S8](../../academy/lessons/S8.md)): bet, wallet, odds and settlement services; what happens when wallet is down.
7. **Java and Spring Boot** ([A7](../../academy/lessons/A7.md), [D4](../../academy/lessons/D4.md), [D8](../../academy/lessons/D8.md)): light touch; Java 17/21 features and Actuator metrics.
8. **OO design and patterns** ([E1](../../academy/lessons/E1.md), [E4](../../academy/lessons/E4.md)): the strategy pattern for settlement rules by bet type.
9. **REST API design and OpenAPI** ([D5](../../academy/lessons/D5.md)): the `POST /bets` contract with an idempotency key and error codes.
10. **Docker and testing** ([D8](../../academy/lessons/D8.md)): a multi-stage image; Testcontainers for Postgres tests.

**Learn** (basic or new for Akhil, core or common here):

1. **Scaling PostgreSQL on Aurora: replicas, failover, partitioning** ([Q8](../../academy/lessons/Q8.md)): read the [replication](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/Aurora.Replication.html) and [HA](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/Concepts.AuroraHighAvailability.html) pages, then add replicas, date partitioning and replica lag to the F8 design.
2. **Event-driven architecture with Kafka: outbox, idempotent consumers, saga** ([S9](../../academy/lessons/S9.md)): learn [topics, partitions and consumer groups](https://kafka.apache.org/intro), then draw `BetPlaced` via an [outbox](https://microservices.io/patterns/data/transactional-outbox.html).
3. **Caching with Redis** ([Q8](../../academy/lessons/Q8.md), [F1](../../academy/lessons/F1.md), [F6](../../academy/lessons/F6.md)): cache-aside vs write-through, TTL and stampede protection ([AWS caching best practices](https://aws.amazon.com/caching/best-practices/)); odds in Redis with a version per selection.
4. **Zero-downtime schema migrations** (no Academy module): expand-migrate-contract for a column rename, and `CREATE INDEX CONCURRENTLY` ([Flyway](https://documentation.red-gate.com/flyway)).
5. **Kubernetes on EKS** (no Academy module): pod, deployment, service, HPA and readiness probe, enough to explain kick-off scaling ([overview](https://kubernetes.io/docs/concepts/overview/)).
6. **Observability** ([D8](../../academy/lessons/D8.md), [F6](../../academy/lessons/F6.md)): RED metrics and trace context; how you find the slow query in production ([OpenTelemetry concepts](https://opentelemetry.io/docs/concepts/)).
7. **WebSockets / SSE for live odds** ([F5](../../academy/lessons/F5.md), [F8](../../academy/lessons/F8.md)): a gateway that fans out from pub/sub; when SSE is enough ([Spring WebSockets](https://docs.spring.io/spring-framework/reference/web/websocket.html)).

## 7. Strategy

**Today is Thu 8 Oct. The round is Tue 13 Oct, 3 PM.** Budget: about 60 min on weekdays and
about 180 min at the weekend (campaign `entain-2026-10`). Academy path: `entain` (Q1–Q8, F1, F3,
F2, S9, F6, F8, where F8 is "design a sports betting platform").

**The last 5 days:**

| Day | Focus (time) |
|---|---|
| **Thu 8 Oct** (60 min) | SQL warm-up: 176, 177, 184, 185 by hand, then check. Read Academy Q5 (indexes and EXPLAIN). |
| **Fri 9 Oct** (60 min) | Transactions: isolation anomalies, MVCC, `FOR UPDATE` vs optimistic locking, deadlocks. Write the wallet-debit transaction and the idempotency key schema on paper. Two SQL problems (180, 1204). |
| **Sat 10 Oct** (180 min) | **Full mock design: "design a sports betting platform / bet placement + wallet + settlement"** in 45 min, out loud, on a whiteboard tool. Then the DB deep dive: schema, indexes per query, partitioning, replicas, cache, outbox. Then 3 SQL problems (262, 550, 601). |
| **Sun 11 Oct** (180 min) | Scaling and performance: read replicas and lag, partitioning vs sharding, N+1 and pagination, pool sizing, online migrations. Second mock: "live odds to 1M clients" (WebSockets fan-out, Redis, Kafka) or `hld/design-distributed-message-queue`. Re-do any SQL you got wrong. |
| **Mon 12 Oct** (60 min, review only) | No new topics. Rehearse the 2-minute "system I built" story with its DB numbers. Run 3 SQL problems from memory. Test the Teams link in the browser and the app. |
| **Tue 13 Oct** (morning) | 20 min: skim §4's database-topic list and your wallet schema. Have a whiteboard tool open and a SQL scratchpad ready. |

**In the room:**

- Ask about scale and consistency needs first.
- Draw the data model before the boxes.
- Say why you chose each index.
- When you write SQL, state your assumptions about ties and NULLs, then test against a tiny
  example table out loud.
- If asked about Entain specifically, mention Aurora Postgres + EKS in one sentence. Don't
  over-claim.

## 8. Sources

All accessed 2026-10-08.

- HR email, 2026-10-08, relayed by Akhil: round-2 focus areas (verified).
- Round-1 invite, Sep 2026, relayed by Akhil: Java Concepts, Coding Concepts, Problem Solving
  (verified).
- Insider tip from a current Entain/Ivy India employee, relayed by Akhil 2026-09-16 (claimed).
- Entain India careers: [home](https://careers.entainindia.com/),
  [sitemap](https://careers.entainindia.com/sitemap/),
  [Hyderabad](https://careers.entainindia.com/locations/hyderabad/);
  [Entain mission & values](https://entaincareers.com/our-offer/mission-values/).
- SmartRecruiters: [SDE III (live)](https://jobs.smartrecruiters.com/Entain/744000154123539-software-development-engineer-iii),
  [Portfolio Lead](https://jobs.smartrecruiters.com/Entain/744000154403050-portfolio-lead); public
  postings API `api.smartrecruiters.com/v1/companies/Entain/postings`.
- Aggregator copies of Entain India postings:
  [SDE II Hyd (Jul 2026)](https://www.talentmate.com/jobs/india/hyderabad/software-development-engineer-ii/2607-11073-77),
  [SDE II Pune](https://builtin.com/job/sde-ii/9861243),
  [SDE II Java (2025)](https://builtin.com/job/p-t-software-development-engineer-ii-java-india/6334894),
  [SDE III (Aug 2026)](https://www.talentmate.com/jobs/india/hyderabad/software-development-engineer-iii/2608-9296-57),
  [SDE III (Sep 2026)](https://www.talentmate.com/jobs/india/hyderabad/software-development-engineer-iii/2609-11073-92),
  [SDE III Fullstack](https://www.talentmate.com/jobs/india/hyderabad/software-development-engineer-iii-fullstack/2607-11073-78),
  [Principal SE](https://simplify.jobs/p/bc2f2849-1b6d-4ae7-ac41-a0cabf650def/Principal-Software-Engineer),
  [Junior Data Engineer](https://www.interviewpal.com/jobs/2243970240),
  [SDE I Java Pune](https://builtin.com/job/p-t-software-development-engineer-i-java-india/6207760).
- [AWS case study: Entain](https://aws.amazon.com/solutions/case-studies/entain-case-study/) (2026).
- [Redpanda / Entain streaming (vendor)](https://whitepapers.theregister.com/paper/view/38707/simplifying-real-time-data-streaming-for-analytics-at-scale).
- [Entain recruiters' interview advice, Apr 2025](https://wherewomenwork.com/Career/6660/Entain-careers-recruitment-team-job-interview-advice).
- Glassdoor (snippets only, pages return 403):
  [SDE 2](https://www.glassdoor.co.in/Interview/Entain-India-SDE-2-Interview-Questions-EI_IE257530.0,12_KO13,18.htm),
  [Entain Hyderabad](https://www.glassdoor.com/Interview/Entain-Hyderabad-Interview-Questions-EI_IE341832.0,6_IL.7,16_IM1076.htm),
  [Senior SE](https://www.glassdoor.com/Interview/Entain-India-Senior-Software-Engineer-Interview-Questions-EI_IE257530.0,12_KO13,37.htm),
  [Entain India all](https://www.glassdoor.co.in/Interview/Entain-India-Interview-Questions-E257530.htm),
  [Test Engineer](https://www.glassdoor.com/Interview/Entain-India-Test-Engineer-Interview-Questions-EI_IE257530.0,12_KO13,26.htm).
- [LeetCode Discuss: Ivy Comptech SSE, Sep 2019](https://leetcode.com/discuss/interview-experience/385015/ivy-comptech-senior-software-engineer-hyderabad-sep-2019-offer).
- Prior research: `DailyProblemTracker-Service/frontend/source/entain-sde2-prep.html`
  (Sep 2026). Its leads were re-checked against their original URLs. Its Glassdoor claims were
  reusable only where a search snippet confirmed them, and its unsourced company figures were not
  carried over.
