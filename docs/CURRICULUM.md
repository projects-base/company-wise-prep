# Academy curriculum

The live curriculum (ids, levels, prerequisites, paths) is `data/academy/curriculum.yaml`; lessons are
`data/academy/lessons/<ID>.md`, written to `docs/LESSON-TEMPLATE.md`. Start with the **Zero to Pro** path.

Lessons are ~25 minutes. Every lesson follows one template:

> Mental model → How it works inside → Lifecycle/diagram → Code lab → **Gotchas** (→ review cards)
> → Interview questions (from the bank, with company tags) → "Explain it in 2 minutes" checkpoint

```
              ┌──────────► B Concurrency ──────────┐
A Java & JVM ─┼──────────► D Spring & Boot ────────┼──► G AI Engineering
              └──► C Time & Memory ◄── DSA bank    │
E SOLID → Patterns → LLD ───────► F HLD / System Design
S  The Story of Backend — the spine; each era links into the tracks
H  Tips & tricks — attached to lessons, not a course
L  Lifecycles — cross-cutting reference
```

## Learning stages

| Stage | Can… | Covers |
|---|---|---|
| 1 Explorer | explain program, memory, network, request | S0, A1 |
| 2 Builder | build a working CRUD app | S1–S7, ShopKart S1–S2, D1–D5 |
| 3 Engineer | make it fast, correct, tested | A2–A6, B, C, D6–D8, ShopKart S3–S4 |
| 4 Senior | survive scale and failure; design LLD | E, B7, D9–D10, ShopKart S5–S6 |
| 5 Architect | choose architectures and defend tradeoffs | F, S8–S13, decision cards, ShopKart S7–S8 |

---

## S — The Story of Backend (pain → invention → new pain)

| Era | Invention | Solved | Created |
|---|---|---|---|
| S0 | Computers, programs, memory, networks, client–server | — | How to serve pages? |
| S1 | CGI — process per request | Dynamic pages | Slow, stateless |
| S2 | Servlets/JSP — thread per request | Process cost | HTML-in-Java spaghetti |
| S3 | MVC & Struts — front controller | Structure | XML, servlet coupling, untestable |
| S4 | J2EE/EJB 2.x | Transactions, remoting | Heavy, untestable |
| S5 | Spring + Hibernate — POJOs, DI, AOP, ORM | EJB weight | XML config, WAR deploys |
| S6 | Annotations, Java config, Maven | XML sprawl | Repeated setup |
| S7 | Spring Boot — auto-config, starters, embedded server | Boilerplate | Monolith growth |
| S7½ | SOA/ESB | Enterprise integration | Central bottleneck |
| S8 | Microservices, Docker, Kubernetes | Team coupling, scaling | Distributed-systems pain |
| S9 | Cloud native — Kafka, mesh, serverless, tracing | Ops at scale | Cost, premature splits |
| S10 | Modular monolith comeback | Accidental complexity | — fit to team and scale |
| S11 | Reactive → virtual threads | Thread exhaustion | — thread-per-request again |
| S12 | AI-native — LLMs, RAG, agents, MCP | Unstructured knowledge | Evals, cost, injection |
| S13 | Next (opinion): agentic systems, Leyden/CRaC, Valhalla, Wasm | — | Judgement over frameworks |

Hands-on labs, break-it exercises, industry cases and the incident library: `docs/STORY-LABS.md`.

Labs reproduce *patterns* (write a mini front controller), never install EOL frameworks
(Struts 2's CVE-2017-5638 → Equifax is itself a lesson).

### ShopKart — one app grown through scale

| Stage | Scale / team | Build | Challenge | Overcome with |
|---|---|---|---|---|
| S1 | 100 / 1 | Servlet + JDBC | No structure | Hand MVC → Spring |
| S2 | 10k / 1–3 | Boot monolith + Postgres | N+1, slow pages | Indexes, fetch joins, pool tuning |
| S3 | 100k | + Redis, CDN | Stale cache, stampede | Cache-aside, TTL+jitter, coalescing |
| S4 | 1M | + replicas, queue | Replica lag, duplicates | Read-your-writes, idempotent consumer, outbox |
| S5 | 1M / 15 | Modular monolith | Teams colliding | Bounded contexts, contract tests |
| S6 | 10M / 50 | Strangler-extract Payments, Inventory | Distributed transactions, cascades | Saga, circuit breaker, bulkhead |
| S7 | 10M+ | Kafka, gateway, tracing, K8s | "Which service is slow?", spikes | Tracing, autoscaling, rate limiting |
| S8 | any | AI search & support | Hallucination, cost | RAG, evals, guardrails |

Each stage ends with an ADR and a "what we'd tell our past self" retro.

### Microservices — principles
Pick architecture for team size and scale (monolith → modular monolith → services) · Conway's law ·
8 fallacies of distributed computing · design for failure · high cohesion/loose coupling ·
database per service · 12-factor · you build it you run it · automate everything ·
**when not to use microservices**.

### Microservices — patterns
| Problem | Patterns |
|---|---|
| Split | Business capability, DDD bounded context, strangler fig, anti-corruption layer |
| Talk | REST/gRPC/events, API gateway, BFF, service discovery |
| Survive | Timeout, retry+backoff+jitter, circuit breaker, bulkhead, fallback |
| Consistency | Saga (choreography/orchestration), outbox, idempotent consumer, CQRS, event sourcing |
| Operate | Central config, health checks, tracing, correlation ids, sidecar/mesh |
| Change | Consumer-driven contracts, versioning, blue-green/canary, feature flags |

### Decision cards (pick when / avoid when / cost / who runs it)
Language (Java, Go, Node, Python, Rust/C++) · Framework (Boot, Quarkus/Micronaut, Jakarta EE) ·
Database (Postgres, MySQL, Mongo, Cassandra/Dynamo, Elasticsearch, pgvector) · Cache (Redis,
Memcached, Caffeine) · Messaging (Kafka, RabbitMQ, SQS) · API (REST, gRPC, GraphQL, WebSockets) ·
Deploy (PaaS, Kubernetes, serverless) · Architecture (monolith → event-driven).

---

## A — Java & JVM
A1 How Java runs (javac → bytecode → class loading → interpreter → JIT C1/C2) ·
A2 Memory areas (heap young/old, metaspace, stacks, code cache, direct) ·
A3 Class loading (delegation, Boot's nested-jar loader) ·
A4 GC (reachability, generations; Serial/Parallel/G1/ZGC) ·
A5 GC diagnosis (logs, jstat, jcmd, heap dumps, MAT, leak patterns) ·
A6 Java Memory Model (happens-before, volatile, safe publication) ·
A7 Versions 8 → 11 → 17 → 21 → 25 ·
A8 Profiling (JFR/JMC, async-profiler, flame graphs, JMH).

## B — Threads & multithreading
B1 Thread lifecycle, interrupts · B2 synchronized, locks, deadlock · B3 Atomics/CAS,
ConcurrentHashMap, queues, latches, semaphores · B4 Executors, pool sizing, ForkJoin ·
B5 CompletableFuture · B6 Virtual threads, pinning, structured concurrency ·
B7 Classic problems (producer-consumer, bounded queue, concurrent LRU, rate limiter).

## C — Time & memory on coding problems
C1 Constraints → complexity (n≤10 n! · ≤20 2ⁿ · ≤500 n³ · ≤5k n² · ≤10⁶ n log n · ≤10⁹ log n) ·
C2 Where memory goes in Java (boxing, HashMap entries, recursion depth, DP rolling arrays,
bitsets, matrix vs list) · C3 Hidden time costs (string concat, remove(0), contains on lists,
autoboxing, sort in loop, missing memo) · C4 Optimisation playbook (brute force → repeated work →
structure → trade memory for time, out loud) · C5 Amortised analysis.

## D — Spring & Spring Boot (scratch → ship a feature)
D1 Build a DI container yourself · D2 Core (IoC, scopes, profiles, Environment) ·
D3 AOP (JDK vs CGLIB proxies; self-invocation trap) · D4 Boot (`@SpringBootApplication`,
auto-config, `@Conditional*`, starters, config order) · D5 Web (request lifecycle, filters vs
interceptors, validation, `@ControllerAdvice`) · D6 Data (persistence context, flush, N+1,
`@Transactional` propagation/isolation/rollback rules) · D7 Security (filter chain, JWT) ·
D8 Production (Actuator, Micrometer, tracing, Testcontainers, slice tests) ·
D9 Extending (BPP, BFPP, ImportSelector, AutoConfiguration.imports, own starter) ·
D10 Ship-a-feature playbook; build Spring from source and trace a request.

Annotation reference grouped by job: stereotypes · DI · config · conditional · web · data ·
async/scheduling/events · validation · testing — each with what it does underneath.

## E — SOLID → patterns → LLD
SOLID as violation → refactor → pattern · GoF 23 (depth on the ~11 used daily), each shown where
Spring/JDK use it · LLD method · cases: parking lot, LRU/LFU, rate limiter, elevator, Splitwise,
BookMyShow, logger, pub-sub, KV store, snake & ladder.

## F — HLD / System design
Building blocks · CAP/PACELC, consistency, Raft, idempotency, back-pressure · method (requirements
→ estimates → API → data → design → deep dives → tradeoffs) · cases: URL shortener, feed, chat,
rate limiter, notifications, payments ledger, autocomplete, distributed cache, video, low-latency
order book · DAGs: workflow orchestration, build systems, schedulers.

## G — AI engineering
G1 How LLMs work · G2 Using models (choice, prompting, structured output, tools, streaming,
caching, batch) · G3 RAG (chunking, embeddings, pgvector, hybrid search, rerank, eval, when not) ·
G4 MCP (servers/clients, tools/resources/prompts; build one over this bank) · G5 Agents & DAG
workflows · G6 Evals & safety · G7 Spring AI.

## L — Lifecycles
Java program · class · object · GC cycle (G1) · thread · Spring bean (instantiate → populate →
Aware → BPP-before → @PostConstruct → afterPropertiesSet → init → BPP-after/proxies → use →
@PreDestroy → destroy) · Boot startup (run → environment → context → auto-config → refresh →
server → runners → ApplicationReady) · HTTP request (filters → DispatcherServlet → mapping →
interceptors → resolvers → controller → converters) · JPA entity · transaction.

## H — Tips & tricks
Attached to lessons as review cards, e.g. `Map.merge`, `Deque` over `Stack`, `Integer.compare`
in comparators, `lo + (hi - lo) / 2`, state the brute force first, check `@Transactional`
boundaries before debugging "not saved", `-XX:+HeapDumpOnOutOfMemoryError`.
