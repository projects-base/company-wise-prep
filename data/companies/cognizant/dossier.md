# Cognizant — interview dossier

Target: **Java MSB + React, Full Stack** (Java + Microservices + Spring Boot + React) at an in-person hiring drive on **Saturday
10 October 2026, 9:30 AM - 1 PM** (Hyderabad, Kokapet). Source: the invite Akhil received
(verified). Researched online on 2026-10-09; everything not from the invite or Cognizant's own
careers site is `claimed` (candidate reports).

## 1. TL;DR

- **The loop is a service-company drive, not a FAANG loop.** Reports describe one or two technical rounds, sometimes a technical-manager round, then HR, all on the same day for walk-ins ([Glassdoor, Pune, Dec 2025](https://clear.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW102631130.htm); [GfG, Jan 2024](https://www.geeksforgeeks.org/cognizant-interview-experience-for-java-spring-boot-full-stack-developer/)). Cognizant's own page adds that some roles include technical assessments and/or **client interviews** ([How we hire](https://careers.cognizant.com/india-en/pathways-to-cognizant/how-we-hire/)).
- **Mostly theory and quick recall:** core Java, collections, exceptions, Java 8 features, Spring Boot, Hibernate, microservices patterns, a little SQL. Answer crisply, then go one level deeper on your own.
- **The one hands-on task most reported is a Java 8 stream program** (even numbers, group anagrams, sort employees, frequency, duplicates). Practise writing them without an IDE.
- **For the Full Stack group, React is where you can lose it:** hooks and lifecycle, functional vs class components, closures/hoisting, Redux Toolkit, and a small React or JS coding task (form with validation, remove duplicates, first non-repeating character).
- **Over-prepare your project story:** architecture on paper (React → gateway → services → DB), one production issue and its fix, why you are changing jobs.

## 2. The loop

| Round | What it assesses | Source |
|---|---|---|
| **Drive day (verified)** — Sat 10 Oct, 9:30 AM - 1 PM, at least 3-4 hours | Skill groups Java MSB and Java MSB + React (Full Stack), 6+ years. Rounds not listed. | Invite relayed by Akhil, 2026-10-09 |
| Technical round 1 (claimed) | Java, Spring Boot, microservices, Kafka, DB — "general questions" | [Glassdoor, Pune weekend interviews, 2025-12](https://clear.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW102631130.htm) |
| Technical round (claimed) | Java 8 features, OOP, collections, exceptions, equals/hashCode, comparators, microservices basics, DROP/DELETE/TRUNCATE, JS arrow functions | [GfG, 2024-01](https://www.geeksforgeeks.org/cognizant-interview-experience-for-java-spring-boot-full-stack-developer/) |
| Long technical (claimed, ~1.5 h) | Java, microservices, Spring Boot, Hibernate + a stream problem | [Glassdoor, 2024-01](https://static.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW84676360.htm) |
| React / JS round (claimed) | JS, React, Redux Toolkit concepts; coding: first non-repeating character | [Glassdoor, front-end walk-in, 2025-05](https://static.glassdoor.at/Interview/Cognizant-Interview-E8014-RVW97532089.htm) |
| React round (claimed, 45 min) | Functional vs class, lifecycle with hooks, closures, hoisting, var/let/const, remove duplicates, React form with validation | [Glassdoor, 2025-03](https://fr.glassdoor.ca/Entretien/Cognizant-Entretien-E8014-RVW75924550.htm) |
| Technical-manager round (claimed) | Project depth and decisions | [Glassdoor, 2025-12](https://clear.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW102631130.htm) |
| HR (claimed) | About yourself, current role, why leave, why Cognizant, notice period, location | [GfG, 2024-01](https://www.geeksforgeeks.org/cognizant-interview-experience-for-java-spring-boot-full-stack-developer/) |

Format notes (claimed): a Feb 2026 Gurgaon candidate had one face-to-face technical round and one HR round at the office ([Glassdoor](https://static.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW84676360.htm)); Nov 2025 Hyderabad walk-in reviewers describe interviews in an open cafeteria with panels side by side and long waits ([Glassdoor](https://static.glassdoor.com.ar/Interview/Cognizant-Interview-E8014-RVW3053764.htm)). Bring water and expect noise.

## 3. The bar

Not published. From the reports, a 6+ year hire answers core Java / Spring Boot / microservices
theory confidently and correctly, writes a short stream solution by hand, and explains their own
project's architecture, trade-offs and one production problem in depth. The Full Stack group adds
React hooks and JavaScript fundamentals at a "can build it" level. Fill in after the drive.

## 4. Question patterns

Ordered by how often the topic shows up across the reports (all `claimed`):

1. **Java 8 features and stream coding** — [java-8-features](../../questions/java/java-8-features.yaml), [java-streams-coding-drills](../../questions/java/java-streams-coding-drills.yaml), [group-anagrams](../../questions/dsa/group-anagrams.yaml)
2. **Collections** — [java-collections-hierarchy](../../questions/java/java-collections-hierarchy.yaml), [hashmap-internals](../../questions/java/hashmap-internals.yaml), [hashmap-vs-hashtable-vs-concurrenthashmap](../../questions/java/hashmap-vs-hashtable-vs-concurrenthashmap.yaml), [comparable-vs-comparator](../../questions/java/comparable-vs-comparator.yaml), [java-equals-vs-double-equals-hashcode](../../questions/java/java-equals-vs-double-equals-hashcode.yaml)
3. **Microservices patterns** — [microservices-design-patterns](../../questions/spring/microservices-design-patterns.yaml) (gateway, Eureka, circuit breaker, saga), [microservices-communication-grpc-vs-rest-eda](../../questions/domain/microservices-communication-grpc-vs-rest-eda.yaml)
4. **Spring Boot and JPA** — [spring-autowired-and-dependency-injection](../../questions/spring/spring-autowired-and-dependency-injection.yaml), [spring-transactional-management](../../questions/spring/spring-transactional-management.yaml), [hibernate-save-vs-persist-vs-update-vs-merge](../../questions/spring/hibernate-save-vs-persist-vs-update-vs-merge.yaml), [oauth-and-jwt](../../questions/domain/oauth-and-jwt.yaml)
5. **Exceptions and OOP** — [java-checked-vs-unchecked-exceptions](../../questions/java/java-checked-vs-unchecked-exceptions.yaml), [polymorphism-and-static-method-overriding](../../questions/java/polymorphism-and-static-method-overriding.yaml)
6. **React and JavaScript** — [react-functional-vs-class-components](../../questions/domain/react-functional-vs-class-components.yaml), [react-hooks-and-redux-toolkit](../../questions/domain/react-hooks-and-redux-toolkit.yaml), [react-form-with-validation](../../questions/domain/react-form-with-validation.yaml), [js-closures-and-hoisting](../../questions/domain/js-closures-and-hoisting.yaml), [js-arrow-functions-and-this](../../questions/domain/js-arrow-functions-and-this.yaml)
7. **Concurrency / JVM** — [java-create-and-fix-deadlock](../../questions/java/java-create-and-fix-deadlock.yaml), [debug-java-memory-issues](../../questions/java/debug-java-memory-issues.yaml)
8. **SQL and Kafka** — [sql-drop-vs-delete-vs-truncate](../../questions/sql/sql-drop-vs-delete-vs-truncate.yaml), [kafka-architecture](../../questions/domain/kafka-architecture.yaml)
9. **Small coding** — [first-unique-character-in-a-string](../../questions/dsa/first-unique-character-in-a-string.yaml)
10. **HR** — [walk-through-current-work](../../questions/behavioral/walk-through-current-work.yaml), [why-are-you-looking-for-a-change](../../questions/behavioral/why-are-you-looking-for-a-change.yaml), [why-cognizant](../../questions/behavioral/why-cognizant.yaml)

## 5. Behavioural

Cognizant's values ([Our culture](https://careers.cognizant.com/india-en/life-at-cognizant/our-culture/)) and the story to map to each:

| Value | Story to use |
|---|---|
| Work as one | A cross-team delivery where you relied on QA / front-end / another service team |
| Raise the bar | A quality improvement you drove (tests, code review standard, performance fix) |
| Dare to innovate | Using AI coding tools or a new approach that sped up delivery |
| Do the right thing | Pushing back on a shortcut that would have hurt data or users |
| Own it | The production issue you owned end to end (also answers "a performance issue you solved") |

HR basics: why leave (growth, bigger full-stack scope — never criticise the employer), why Cognizant (scale of client work, Java + React full-stack roles), notice period, Hyderabad location.

## 6. Company-specific angle

Cognizant staffs client projects, so interviewers check that you can be put on a client project
quickly: breadth across the stack, clear communication, and real project experience. Expect
"how did you do it in your project" follow-ups after each theory question, and a possible later
client interview before an offer (Cognizant's own page says some roles include client interviews).

## 6b. What the job postings ask for

Four current-or-recent Cognizant postings for this profile (pages 404'd on fetch, details from
search snippets, 2026-10-09): Java Fullstack Developer (PAN India, 6-12 y), Sr. Java Full Stack
Developer React (6-10 y), Senior Full Stack Data Engineer Java + Spring Boot + React + ETL
(Hyderabad, 6-10 y), Technical Lead Java + React (Hyderabad among 4 cities, 7-12 y).

| Skill | Postings |
|---|---|
| Java, Spring Boot, Microservices, React | 4/4 |
| REST APIs | 3/4 |
| SQL / PL-SQL | 2/4 |
| Cloud (AWS/Azure/GCP) | 2/4 |
| Hibernate/JPA, ETL | 1/4 |

Beyond DSA this implies: REST API design and error handling, cloud deployment talk, and SQL.

## 6c. Tech stack and frameworks to prepare

**Frameworks & languages:** Java 8+ (core), Spring Boot (core), React.js (core), JavaScript (core), Hibernate/JPA (common), SQL/PL-SQL (common), AWS/Azure/GCP (common), Kafka (mentioned).
**Patterns:** microservices architecture (core); API gateway, service discovery, circuit breaker, saga (common); JWT/Spring Security (mentioned).
**Tools:** none named consistently in the sources.

**Revise** (Akhil has these; refresh for depth)
1. React hooks and JS fundamentals — useEffect as lifecycle, memo hooks, Context vs Redux Toolkit; closures, hoisting, event loop. Build a fetch-to-table and a validated form. Academy: none (no React track yet).
2. Java 8 streams and collections internals — 8 drills by hand; HashMap vs Hashtable vs ConcurrentHashMap. Academy: A7, H1, B3.
3. Spring Boot internals and transactions — auto-config, DI, @Transactional propagation and self-invocation. Academy: D4, D3, D6, L4.
4. Microservices patterns — your project with gateway, discovery, config, Resilience4j. Academy: S8, S9.
5. Hibernate/JPA — entity states, save/persist/merge, N+1. Academy: D6, L7.
6. SQL basics — DROP/DELETE/TRUNCATE, joins, second highest, max per department. Academy: Q1, Q2.

**Learn** (basic for Akhil, asked here)
1. Saga and event-driven design — choreography vs orchestration with compensations, on paper. Academy: S9.
2. Cloud deployment basics (AWS) — how a Spring Boot + React app is deployed (container, ECS/EKS, RDS, S3 + CloudFront). Academy: none.
3. Kafka basics — topics, partitions, consumer groups, offsets, idempotent consumers. Academy: S9 (no dedicated Kafka module).

## 7. Strategy

One evening before the drive. Use the revision table below, the bank questions in section 4 in
that order, and the campaign `cognizant-2026-10`. On the day: arrive early (walk-ins queue), carry
the documents listed in the invite, answer every theory question with a one-line definition +
how you used it in your project, and write stream code neatly on paper.

### What the invite says

- **Skill groups:** Java MSB (6+ years) and Java MSB + React, Full Stack (6+ years). Akhil is in
  the Full Stack group, so expect React and JavaScript questions alongside Java.
- **Duration:** at least 3-4 hours. The rounds are not listed.
- **Bring:** printed resume, original government ID, printed latest compensation/increment letter,
  and the CID from the invite.

### Revision for the night before (plan, not research)

| Time | Topic | Cover |
|---|---|---|
| 45 min | React and JavaScript | hooks (`useState`, `useEffect` dependencies and cleanup, `useMemo`, `useCallback`, `useRef`, `useContext`), custom hooks, virtual DOM and keys, controlled inputs, lifting state, Context vs Redux Toolkit, React Router, `React.memo`, error boundaries, StrictMode double effects, React 18/19 batching and `useTransition`; JS closures, event loop, promises and async/await, `this`, hoisting, `==` vs `===`, destructuring and spread, debounce; build a fetch-and-list component with loading and error states |
| 30 min | Java 8 streams by hand | character frequency, duplicates, second highest, group by department, max salary per department, first non-repeated character, `partitioningBy`, `flatMap`, laziness |
| 25 min | Core Java | `HashMap` internals, `ConcurrentHashMap`, `equals`/`hashCode`, immutability and the String pool, exceptions, `ExecutorService`, `CompletableFuture`, `volatile`, Java 17/21 features |
| 35 min | Spring Boot and microservices | auto-configuration, bean scopes and lifecycle, `@Transactional` propagation and self-invocation, `@ControllerAdvice`, profiles, Actuator; API gateway, discovery, config server, circuit breaker, retries and timeouts, Feign/WebClient, saga, tracing, JWT/OAuth2 |
| 10 min | JPA and SQL | N+1 and fixes, lazy vs eager, caches; second-highest salary, max per department, duplicates, joins |
| 10 min | Your story | 90-second intro, current project architecture on paper (UI, API, services, database), one production issue |

Academy modules that cover the Java side: the Java, Spring and Databases & SQL tracks. The Academy has no React track yet.

## 8. Sources

All accessed 2026-10-09. Glassdoor, AmbitionBox and Naukri pages blocked direct fetching (403),
so their content comes from search-engine snippets; dates are the month of the report.

- Interview invite relayed by Akhil (verified), 2026-10-09
- [Cognizant careers — How we hire](https://careers.cognizant.com/india-en/pathways-to-cognizant/how-we-hire/) (official)
- [Cognizant careers — Our culture](https://careers.cognizant.com/india-en/life-at-cognizant/our-culture/) (official)
- Postings (official, 404 on fetch): [00067885132](https://careers.cognizant.com/india-en/jobs/00067885132/java-fullstack-developer/), [00068625692](https://careers.cognizant.com/us-en/jobs/00068625692/sr-java-full-stack-developer-react/), [00068542621](https://careers.cognizant.com/us-en/jobs/00068542621/senior-full-stack-data-engineer-java-plus-spring-boot-plus-react-js-plus-etl/), [00066236781](https://careers.cognizant.com/ca-en/jobs/00066236781/technical-lead-java-plus-react/)
- [GeeksforGeeks — Java Spring Boot Full Stack Developer, 2024-01](https://www.geeksforgeeks.org/cognizant-interview-experience-for-java-spring-boot-full-stack-developer/) (fetched)
- [Naukri Code360 — 2024-07](https://www.naukri.com/code360/interview-experiences/cognizant/cognizant-interview-experience-by-koushal-goyal-jul-2024-exp-0-2-years)
- Glassdoor: [Pune 2025-12](https://clear.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW102631130.htm), [New Delhi 2021-12](https://static.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW74541668.htm), [Bangalore 2024-01 / Gurgaon 2026-02](https://static.glassdoor.nl/Interview/Cognizant-Interview-E8014-RVW84676360.htm), [Calcutta 2025-08](https://www.glassdoor.com.ar/Entrevista/Cognizant-Entrevista-E8014-RVW100139680.htm), [Bengaluru 2025-08](https://static.glassdoor.it/Interview/Cognizant-Software-Developer-Interview-Questions-EI_IE8014.0,9_KO10,28_IP20.htm), [React 2025-03](https://fr.glassdoor.ca/Entretien/Cognizant-Entretien-E8014-RVW75924550.htm), [front-end walk-in 2025-05](https://static.glassdoor.at/Interview/Cognizant-Interview-E8014-RVW97532089.htm), [Hyderabad walk-in 2025-11](https://static.glassdoor.com.ar/Interview/Cognizant-Interview-E8014-RVW3053764.htm)

## 9. What was asked (fill in after the drive)

_Tell Claude the questions from each round; they go into the bank as `verified` sightings._
