# Story of Backend — labs, challenges and industry cases

The practice side of track S in `CURRICULUM.md`. Every era is learned through five kinds of work:

| Kind | What you do | Why it sticks |
|---|---|---|
| **Build** | Implement the era's idea yourself, small | You understand what the framework hides |
| **Break** | Push it until it fails, measure the failure | You feel the pain that caused the next era |
| **Case** | Read a real company's migration or outage, answer the questions | You see the tradeoff at industry scale |
| **Debate** | Argue both sides, write a one-page ADR | Interviews test reasoning, not recall |
| **Drill** | Answer the interview question in 2 minutes, out loud | Converts understanding into delivery |

Case studies are named by title, company and year so you can find the original write-up —
read the original, not a summary of it, before treating a claim as fact.

---

## S1 · CGI — a process per request

- **Build:** A tiny HTTP server (Java `ServerSocket`) that handles each request by launching a new
  OS process (`ProcessBuilder`) that prints the page.
- **Break:** Load-test with 200 concurrent users (`hey` / `wrk` / JMeter). Record latency and
  CPU. Then switch to a thread per request and compare.
- **Case:** Early web servers (NCSA httpd) and Perl CGI scripts; why FastCGI and `mod_perl` appeared.
- **Tradeoff:** Process isolation (a crash kills one request) vs process start-up cost and no
  shared memory.
- **Overcome:** Long-lived workers (FastCGI) → in-process threads (Servlets).
- **Drill:** "Why is process-per-request slow, and what does a thread save you?"

## S2 · Servlets & JSP — a thread per request

- **Build:** A raw servlet on embedded Jetty/Tomcat: login, session, list orders.
- **Break:** Store a counter in a servlet **instance field**, hit it with 100 threads, watch the
  count go wrong. (Servlets are singletons — the classic concurrency bug.) Then put HTML in Java
  and Java in JSP for 5 pages and try to change the layout.
- **Case:** Sun's Java Web Server (1997), Apache Tomcat (1999) — why Java took enterprise web.
- **Tradeoff:** Cheap threads and shared memory vs shared-state bugs and mixed concerns.
- **Overcome:** Fix the counter (`AtomicLong`, then no shared state at all); separate concerns → MVC.
- **Drill:** "Is a servlet thread-safe? What happens to instance variables?"

## S3 · MVC & Struts — the front controller

- **Build:** Your own front controller: one servlet, an `actions.properties` mapping
  `/orders/create → CreateOrderAction`, a view resolver. (Do not install Struts — it is EOL.)
- **Break:** Add 15 actions. Then try to unit-test one without a servlet container.
- **Case:** Apache Struts in 2000s enterprise apps; **Equifax 2017** — breached through an
  unpatched Apache Struts 2 vulnerability (CVE-2017-5638).
- **Tradeoff:** Structure and convention vs XML sprawl, servlet-API coupling, poor testability.
  Frameworks also become attack surface you must patch.
- **Overcome:** POJOs + dependency injection; dependency scanning and patch discipline.
- **Debate:** "Your 2012 app is on Struts 2. Patch, upgrade, or rewrite?" — write the ADR.

## S4 · J2EE / EJB 2.x — the heavyweight container

- **Build:** Write (don't run) the EJB 2 shape for one service: home interface, remote
  interface, bean class, deployment descriptor, JNDI lookup. Count files and lines.
- **Break:** Compare with the same service as one plain class. List what you can't do without the
  container (test, run in `main`).
- **Case:** Banks and telcos on WebLogic/WebSphere; **EJB 3.0 (2006)** adopting POJOs and
  annotations — the standard copying the alternatives (Spring, Hibernate).
- **Tradeoff:** Declarative transactions, security, remoting vs weight, slow deploy, untestable.
- **Overcome:** Keep the services (transactions, security), drop the weight → Spring.
- **Drill:** "What did EJB get right that Spring kept?"

## S5–S6 · Spring + Hibernate → annotations

- **Build:** Lab D1 — your own DI container (~150 lines). Then port the S3 app to Spring XML,
  then to annotations/Java config. Add Hibernate for orders.
- **Break:** Trigger the N+1 query problem with a lazy collection; log the SQL; fix it with a fetch
  join. Call a `@Transactional` method from the same class and watch the transaction not start.
- **Case:** Rod Johnson's *Expert One-on-One J2EE Design and Development* (2002) → Spring 1.0
  (2004); Hibernate (2001) → standardised as JPA (2006).
- **Tradeoff:** Testability and POJOs vs proxy "magic" you must understand to debug.
- **Overcome:** Learn the proxy model (D3) — most Spring surprises are proxy surprises.
- **Drill:** "Why doesn't `@Transactional` work on a self-invocation?"

## S7 · Spring Boot

- **Build:** Port the app to Boot. Count configuration lines before/after. Then write your **own
  auto-configuration + starter** (D9) and switch it off with a property.
- **Break:** Two starters that each define a `DataSource` — read the condition evaluation report
  (`--debug`) to see who won and why.
- **Case:** Spring Boot 1.0 (2014); convention over configuration borrowed from Rails (2004);
  the fat jar fitting the container era (Docker, 2013).
- **Tradeoff:** Speed of start vs "what is configured and where?" — the answer is always in the
  auto-config report.
- **Overcome:** `--debug`, `/actuator/conditions`, reading auto-config source.
- **Drill:** "What happens between `SpringApplication.run` and the first request?"

## S7½ · SOA & ESB

- **Debate:** Draw ShopKart integrated through a central ESB doing routing + transformation +
  business rules. Then list what breaks when the ESB team is the bottleneck for every change.
- **Case:** Enterprise SOA programs of the 2000s; the "smart endpoints, dumb pipes" reaction
  (Lewis & Fowler, *Microservices*, 2014).
- **Tradeoff:** Central governance vs central bottleneck.

## S8 · Microservices

- **Build:** Strangler-fig ShopKart: route `/payments/**` to a new service via a gateway, keep the
  rest in the monolith. Give Payments its own database.
- **Break:** Make Payments slow (sleep 5s) — watch the monolith's threads exhaust (cascading
  failure). Kill Payments mid-order — find the half-finished order.
- **Overcome:** Timeouts + circuit breaker + bulkhead (Resilience4j); saga with compensation;
  transactional outbox; idempotency keys.
- **Cases:**
  - **Amazon** — the early-2000s move to services behind APIs and small "two-pizza" teams.
  - **Netflix** — the 2008 database corruption that stopped DVD shipping for days, the move to
    AWS and services; **Chaos Monkey (2011)**; **Hystrix** (circuit breaker) → maintenance mode
    (2018) → Resilience4j.
  - **Uber** — monolith → thousands of microservices → *Domain-Oriented Microservice
    Architecture* (2020) to tame the sprawl.
- **Tradeoff:** Independent deploy and scaling vs network failures, consistency, ops cost.
- **Debate:** "15 engineers, one product — microservices?" Write the ADR both ways.

## S9 · Cloud native & event-driven

- **Build:** Order placed → Kafka event → Inventory and Email consume it. Add tracing
  (OpenTelemetry) across all three.
- **Break:** Deliver the same event twice; consume out of order; let a consumer lag 10 minutes.
- **Overcome:** Idempotent consumers, partition keys for ordering, dead-letter topics, lag alerts.
- **Cases:** **LinkedIn** built Kafka (open-sourced 2011); **Kubernetes (2014)** grew out of
  Google's Borg; **Discord** — *How Discord Stores Trillions of Messages* (Cassandra → ScyllaDB, 2023).
- **Tradeoff:** Decoupling and replay vs eventual consistency and harder debugging.

## S10 · The pendulum — modular monoliths

- **Build:** Merge two over-split ShopKart services back into one module with Spring Modulith;
  verify module boundaries in a test.
- **Cases:**
  - **Segment** — *Goodbye Microservices* (2018): many destination services merged back.
  - **Shopify** — *Deconstructing the Monolith* (2019): a componentised monolith instead of services.
  - **Amazon Prime Video** (2023) — a monitoring pipeline moved from distributed serverless
    components into one process, cutting cost sharply.
  - **Stack Overflow** — very large traffic on a small number of servers and a monolith.
- **Debate:** "When is splitting a service the wrong answer?"

## S11 · Reactive → virtual threads

- **Build:** The same blocking endpoint three ways: platform thread pool, WebFlux, virtual threads.
  Load-test all three. Compare throughput and **how readable the code and stack traces are**.
- **Break:** Pin a virtual thread with `synchronized` around blocking I/O (Java 21) and watch the
  carrier threads run out.
- **Cases:** **Netflix** — *Java 21 Virtual Threads – Dude, Where's My Lock?* (2024), a pinning
  deadlock in production; **Discord** — *Why Discord is switching from Go to Rust* (2020), GC
  latency spikes as the trigger.
- **Tradeoff:** Throughput vs debuggability; GC languages vs manual memory for latency.

## S12 · AI-native

- **Build:** RAG over this repo's question bank (pgvector); an MCP server exposing the bank (G4).
- **Break:** Ask questions the bank can't answer — measure how often the model invents one.
  Plant an instruction inside a document and see if it gets followed (prompt injection).
- **Overcome:** Retrieval evals, "answer only from sources", citations, input/output guardrails.
- **Cases:** *Retrieval-Augmented Generation* (Lewis et al., 2020); **Air Canada chatbot ruling
  (2024)** — the airline was held to a refund policy its chatbot made up; **MCP** (Anthropic, 2024).
- **Tradeoff:** Capability vs non-determinism, cost and liability.

---

## Incident library — trade-offs learned the expensive way

Each: read the post-mortem → name the root cause → map it to a lesson → write how you'd prevent it.

| Incident | Lesson | Track |
|---|---|---|
| **Knight Capital (2012)** — ~$440M lost in under an hour after a deployment reactivated dead code | Deploy safety, feature flags, removing dead code | S8, deploy |
| **Cloudflare (July 2019)** — a regex with catastrophic backtracking pegged CPU worldwide | Time complexity in production | C3 |
| **GitLab (2017)** — production database deleted; several backups turned out not to work | Test your restores | F |
| **AWS S3 us-east-1 (2017)** — a mistyped command removed more capacity than intended | Blast radius, guarded tooling | F |
| **Facebook (Oct 2021)** — a BGP change took its services and internal tools offline | Don't let recovery depend on the thing that is down | F |
| **CrowdStrike (July 2024)** — a faulty content update crashed Windows hosts globally | Staged rollout, canaries | S8 |
| **Netflix virtual-thread deadlock (2024)** | Know pinning before adopting virtual threads | B6 |

## Capstone challenges

1. **Time machine:** ShopKart's "place order" built in S2, S3, S5, S7, S8 styles; one table comparing
   lines of code, test time, start-up time, throughput.
2. **Scale ladder:** take ShopKart from S1 to S7 (CURRICULUM.md), one ADR per stage.
3. **Game day:** someone (or a script) injects a failure — slow DB, dead service, duplicate events,
   full disk — and you find and fix it using only logs, metrics and traces.
4. **Defend it:** pick a case study, argue the opposite decision for 5 minutes, then argue theirs.
