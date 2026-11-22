# AI Build: TriageDesk (the spec every milestone follows)

TriageDesk is one Spring Boot service Akhil grows over 10 sessions, in **his own separate repo** (not this one).
Each milestone is `data/academy/lessons/P<n>.md` (track `P`, code `AI-BUILD`). It is split into 2–3 build sessions
of 30–60 minutes. The dated plan books one hour for it, so expect to carry the remaining sessions into the next days'
stretch time. It applies only what the Academy has taught by then, mostly the AI-ENG track. The project gives real,
defensible answers to "have you built anything with LLMs, MCP, Terraform?" It is a **personal project**. Say so in
interviews, and never present it as production work for an employer.

## The product

Engineers report **incidents** (a failing deploy, a slow database, a certificate that expired). TriageDesk:

1. **Ingests** incidents through a REST API (milestone 1), and later from Kafka (milestone 8).
2. **Triages** each one with an LLM: a category, a suggested severity, a probable cause and a one-paragraph summary,
   returned as **structured output** (milestone 2).
3. **Finds similar past incidents** and how they were fixed, using embeddings in pgvector (RAG), and gives them to the
   model as context (milestone 3).
4. Shows a **dashboard**: the triage queue, an incident page with similar incidents, and stats (milestone 4).
5. Exposes `search_incidents`, `get_incident` and `triage_incident` through an **MCP server**, so an AI client
   (Claude Code, Claude Desktop, or the MCP Inspector) can use them (milestone 5).
6. **Measures** classification accuracy on a golden set with evals, and **redacts PII** before any text reaches a model
   (milestone 6).
7. Runs an **agent** that auto-triages with tool calls. Risky actions wait for **human approval** (milestone 7).
8. Ingests **asynchronously** through Kafka and a transactional outbox, and wraps the LLM in **Resilience4j** (timeout,
   retry with backoff, circuit breaker, fallback to "needs human") (milestone 8).
9. Is **observable**: latency, token and cost metrics, traces, and a Grafana board (milestone 9).
10. **Ships** with Docker and Terraform on AWS, a budget alert and `terraform destroy` (milestone 10).

The domain is generic IT incidents. It contains no patient data and no employer data. All test and seed data is
**synthetic**: you write it, or you generate it and review it.

## Fixed decisions (keep every milestone consistent)

### Stack and versions

Checked on 22 Nov 2026 against Maven Central, the spring.io support API and the released jars (sources below).
Re-check before you start, because these projects move fast.

| Piece | Version / artifact | Notes |
|---|---|---|
| Java | 21 | records, pattern matching, virtual threads (Spring AI 2.0 needs 17+) |
| Spring Boot | **4.1.3** (parent `spring-boot-starter-parent` 4.1.3) | OSS support to 31 Jul 2027; see "Why 4.1 and not 4.0" |
| Spring AI | **2.0.3**, through the BOM `org.springframework.ai:spring-ai-bom` | built against Boot 4.1.3; supports Boot 4.0.x and 4.1.x |
| Web | `spring-boot-starter-webmvc` | Boot 4 name; `spring-boot-starter-web` was the Boot 3 name |
| Chat model, primary | `spring-ai-starter-model-anthropic` | Claude, on the official Anthropic Java SDK; key from `ANTHROPIC_API_KEY` |
| Chat model, free local option | `spring-ai-starter-model-ollama` | Ollama on `http://localhost:11434` |
| Embeddings | `spring-ai-starter-model-transformers` | local ONNX model; default `all-MiniLM-L6-v2`, **384 dimensions** |
| Vector store | `spring-ai-starter-vector-store-pgvector` | table `vector_store`, created by Flyway, not by Spring AI |
| MCP server | `spring-ai-starter-mcp-server-webmvc` | MCP Java SDK 2.1.0; Streamable HTTP is the default protocol, endpoint `/mcp` |
| Database | PostgreSQL 16 + pgvector, image `pgvector/pgvector:pg16` | from milestone 1, so nothing changes in milestone 3 |
| Migrations | `spring-boot-starter-flyway` + `flyway-database-postgresql` (Flyway 12.4) | Hibernate only validates (`ddl-auto: validate`) |
| JSON | Jackson 3 (`tools.jackson`, `JsonMapper` bean) | Boot 4 default; annotations stay in `com.fasterxml.jackson.annotation` |
| Tests | JUnit 5, AssertJ, Mockito, `spring-boot-starter-webmvc-test`, Testcontainers 2.0 (`testcontainers-postgresql`, `testcontainers-kafka`), Awaitility | no test calls a real LLM |
| Dashboard | Thymeleaf (`spring-boot-starter-thymeleaf`) | server-rendered, minimal JS |
| Messaging | `spring-boot-starter-kafka` (kafka-clients 4.2); KRaft single broker, `apache/kafka:3.9.0` in Compose | milestone 8 |
| Resilience | `io.github.resilience4j:resilience4j-spring-boot4` **2.4.0** | milestone 8 |
| Observability | Actuator, `micrometer-registry-prometheus`, `spring-boot-starter-opentelemetry` (OTel bridge + OTLP exporter) | Prometheus, Grafana and Jaeger in Compose |
| Infrastructure | Terraform with the `hashicorp/aws` provider | milestone 10 |

**Why 4.1 and not 4.0.** Spring Boot 4.0.x open-source support ends on 31 Dec 2026, before the first milestone
(1 Feb 2027). Boot 4.1.x is supported to 31 Jul 2027, which covers the whole build (to 7 Apr 2027), and Spring AI
2.0.3 is itself built against Boot 4.1.3. Boot 4.2.0 and Spring AI 2.1.0 are already on Maven Central at this check,
but they are days old; too new to build a course on. Revisit after milestone 10.

#### Verified, not guessed

Every Java, YAML and SQL snippet in the milestones comes from a throwaway Maven project that contains the code of all
ten milestones and was compiled and tested on Java 21 with the versions above: 70 unit and slice tests green with
`mvnw -q test`, including a full-context wiring test (it caught a startup bean cycle, fixed in milestone 5), an MCP
smoke test over real HTTP (initialize, session id, `tools/list`), and a test that checks every Spring property in the
milestone YAML against the configuration metadata in the Boot and Spring AI jars (it fails on deprecated or unknown
names). The `*IT` tests (Postgres, pgvector, Kafka through Testcontainers) compile but need Docker, so they have not
been run; run them in your own repo. Declare `maven-failsafe-plugin` in your pom (milestone 1): the Boot parent only
manages it, and without it `./mvnw verify` silently skips every `*IT`.

Sources:

- Maven Central metadata: `https://repo1.maven.org/maven2/org/springframework/boot/spring-boot/maven-metadata.xml`,
  `https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-bom/maven-metadata.xml`,
  `https://repo1.maven.org/maven2/io/github/resilience4j/resilience4j-spring-boot4/maven-metadata.xml`
- Support windows: `https://api.spring.io/projects/spring-boot/generations` and
  `https://api.spring.io/projects/spring-ai/generations` (the data behind `spring.io/projects/spring-boot#support`)
- Spring AI 2.0.3 build (Boot 4.1.3, MCP SDK 2.1.0, Anthropic SDK): `https://github.com/spring-projects/spring-ai/blob/v2.0.3/pom.xml`
- Boot 4.1.3 managed versions and starter names:
  `https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.3/spring-boot-dependencies-4.1.3.pom`
- API and property details: the Spring AI 2.0.3 `-sources.jar` files on Maven Central and the
  `spring-configuration-metadata.json` inside the jars. Reference docs: `https://docs.spring.io/spring-ai/reference/`.

#### What changed from Boot 3.5 + Spring AI 1.1 (each one is taught where it first matters)

| Area | Boot 3.5 / Spring AI 1.1 | Boot 4.1 / Spring AI 2.0 | Milestone |
|---|---|---|---|
| Starters | `-web`, `flyway-core` | `-webmvc`, `spring-boot-starter-flyway`, `spring-boot-starter-kafka`, `-opentelemetry` | 1, 8, 9 |
| Test slices | `TestRestTemplate`, `o.s.b.test.autoconfigure.web.servlet.WebMvcTest` | `RestTestClient` + `@AutoConfigureRestTestClient`, `o.s.b.webmvc.test.autoconfigure.WebMvcTest` | 1 |
| Testcontainers | 1.x, `PostgreSQLContainer<?>` in `org.testcontainers.containers` | 2.0, `org.testcontainers.postgresql.PostgreSQLContainer` (not generic) | 1 |
| JSON | Jackson 2 `ObjectMapper`, checked `JsonProcessingException` | Jackson 3 `JsonMapper`, unchecked `JacksonException` | 8 |
| Anthropic properties | `spring.ai.anthropic.chat.options.model` etc. | `spring.ai.anthropic.chat.model`, `.max-tokens`, `.temperature` (old names deprecated) | 2 |
| Anthropic client | Spring `RestClient`, `spring.ai.retry.*`, `TransientAiException` | official Anthropic Java SDK: `spring.ai.anthropic.timeout` (60 s), `max-retries` (2), SDK exceptions | 2, 8 |
| Anthropic defaults | max tokens 500 | model `claude-haiku-4-5`, max tokens 4096 | 2 |
| Structured output | `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` | same, plus per call `entity(Class, spec -> spec.useProviderStructuredOutput())` | 2 |
| Tool loop | `ToolCallingChatOptions.internalToolExecutionEnabled(false)` | removed: a `ChatModel` never runs tools; `ChatClient`'s `ToolCallingAdvisor` does | 7 |
| MCP server | `protocol: STREAMABLE` had to be set | Streamable HTTP is the default; MCP SDK 2.1 (protocol up to `2025-11-25`) | 5 |
| Resilience4j | `resilience4j-spring-boot3` | `resilience4j-spring-boot4` | 8 |
| OTLP tracing | `management.otlp.tracing.endpoint` | `management.opentelemetry.tracing.export.otlp.endpoint` | 9 |

### Model choice is configuration (Strategy)

- Both chat starters are on the classpath. `spring.ai.model.chat` picks one: `anthropic` by default, and `ollama`
  under the `local-llm` profile. Spring AI's `ChatModel` is the **Strategy** interface. Auto-configuration creates
  only the selected implementation.
- Each provider has its own option names. Anthropic in Spring AI 2.0: `spring.ai.anthropic.chat.model`,
  `.max-tokens`, `.temperature`, plus client settings `spring.ai.anthropic.timeout` and `spring.ai.anthropic.max-retries`
  (set to `0`: Resilience4j owns retries from milestone 8). Ollama: `spring.ai.ollama.chat.model`, and Spring AI's own
  `spring.ai.retry.max-attempts` (set to `1`).
- `spring.ai.model.embedding=transformers` is always set explicitly. The Ollama starter can also auto-configure an
  embedding model, and two embedding beans would clash.
- Anthropic has no embeddings API. Embeddings come from the local ONNX model, so they are free and need no network
  after the first download. **The vector dimension is fixed by the embedding model**: `all-MiniLM-L6-v2` produces
  384 numbers, so the column is `vector(384)` and `spring.ai.vectorstore.pgvector.dimensions=384`. If you switch to
  Ollama embeddings (for example `nomic-embed-text`, 768 dimensions; check the model card), you need a new
  migration for the column, and you must re-embed every document. Vectors from different models cannot be compared
  (AI-ENG-3).

### Package layout (package-by-feature)

```text
com.triagedesk
├── TriageDeskApplication
├── common       TriageDeskException, ApiExceptionHandler (problem details), ClockConfig,
│                guard/ (PII redaction chain, milestone 6), outbox/ (milestone 8)
├── incident     Incident, IncidentStatus, Severity, IncidentRepository, IncidentService, IncidentController,
│                ingest/ (Kafka inbound listener, milestone 8)
├── triage       TriageResult, Category, TriageClassifier (port), SpringAiTriageClassifier (adapter),
│                TriagePipeline, Triage (entity), TriageService, TriageController, ResilientTriageClassifier
├── similarity   IncidentIndexer, SimilarIncidentFinder, SimilarIncident
├── dashboard    DashboardController, StatsQuery, templates under resources/templates
├── mcp          IncidentMcpTools, McpToolsConfig
├── agent        AutoTriageAgent, AgentTools, PendingAction, ActionHandler (+ implementations), ActionController
└── eval         EvalRunner, EvalCase, EvalScorer (profile "eval" only)
```

Features talk to each other through public services (`IncidentService`, `TriageService`, `SimilarIncidentFinder`),
never through another feature's repository.

### Domain model

- `Severity { SEV1, SEV2, SEV3, SEV4 }`. SEV1 is the most severe.
- `IncidentStatus { NEW, TRIAGED, NEEDS_HUMAN, RESOLVED }`. Allowed transitions: `NEW → TRIAGED | NEEDS_HUMAN |
  RESOLVED`, `TRIAGED → TRIAGED (re-triage) | NEEDS_HUMAN | RESOLVED`, `NEEDS_HUMAN → TRIAGED | RESOLVED`.
  `RESOLVED` is terminal.
- `Incident(id UUID, title, description, service, severity, createdAt, status, resolutionNotes, resolvedAt, version)`.
  This is a JPA entity with identity equality, `@Version Long version` for optimistic locking, and time from an
  injected `Clock`. Milestone 8 adds `sourceKey` (unique, nullable), used to deduplicate Kafka ingestion.
- `Category { DATABASE, NETWORK, DEPLOYMENT, CONFIGURATION, CAPACITY, DEPENDENCY, SECURITY, APPLICATION_BUG, OTHER }`.
- `TriageResult(category, suggestedSeverity, probableCause, summary)`: the structured-output record the model fills.
- `Triage`: one row per triage attempt. It holds the `TriageResult` fields, plus `outcome` (`LLM`, `AGENT`, or
  `FALLBACK`), `failureCode`, `model`, `promptVersion`, `inputTokens`, `outputTokens`, `latencyMs` and `createdAt`.
  The latest row is the incident's current triage.
- `PendingAction(id, incidentId, type, payload, status, reason, createdAt, decidedAt)`, from milestone 7. The types
  are `ESCALATE_SEVERITY` and `MARK_DUPLICATE`, and the statuses are `PENDING`, `APPROVED` and `REJECTED`. It is a
  stored **Command**: proposed by the agent, executed only when a human approves it.
- Errors: one unchecked base `TriageDeskException` with a stable `code()`. `IncidentNotFoundException`
  (`INCIDENT_NOT_FOUND`, 404) and `InvalidStatusTransitionException` (`INVALID_STATUS_TRANSITION`, 409) start in
  milestone 1. Milestone 2 adds `TriageFailedException` (codes `LLM_TIMEOUT`, `LLM_ERROR`, `INVALID_OUTPUT`). It is
  internal and never reaches HTTP, because it becomes a `FALLBACK` triage. Milestone 7 adds
  `ActionNotFoundException` (`ACTION_NOT_FOUND`, 404) and `ActionAlreadyDecidedException`
  (`ACTION_ALREADY_DECIDED`, 409). Milestone 8 adds `LlmUnavailableException` (`LLM_UNAVAILABLE`, internal).

### REST API (base `/api/v1`) and UI

- `POST /incidents` with body `{title, description, service, severity}` → `201` + `Location`
- `GET /incidents?status=&page=&size=` · `GET /incidents/{id}` (includes the latest triage from milestone 2)
- `POST /incidents/{id}/triage` → runs the triage pipeline now, returns the triage (milestone 2)
- `POST /incidents/{id}/resolve` with body `{resolutionNotes}` → `200`, and the incident is indexed for similarity
  search (milestone 3)
- `GET /incidents/{id}/similar?k=5` (milestone 3) · `POST /admin/reindex` (milestone 3)
- `POST /incidents/{id}/agent-runs` (milestone 7) · `GET /actions?status=PENDING` · `POST /actions/{id}/approve` ·
  `POST /actions/{id}/reject` (milestone 7)
- UI (milestone 4): `GET /` (queue), `GET /ui/incidents/{id}`, `GET /ui/stats`
- MCP (milestone 5): Streamable HTTP at `/mcp`
- Errors are RFC 9457 problem details with a `code` property.

### Tables (Flyway)

`V1__incident.sql` (milestone 1) · `V2__triage.sql` (milestone 2) · `V3__vector_store.sql` (milestone 3: the
extensions plus `vector_store` with `embedding vector(384)` and an HNSW index) · `V4__agent.sql` (milestone 7:
`pending_action`, `agent_run`) · `V5__outbox.sql` (milestone 8: `outbox_event`, plus `incident.source_key`).

### Kafka (milestone 8)

- `triagedesk.incidents.inbound`: other systems publish raw incidents here. The listener is idempotent through
  `source_key`.
- `triagedesk.incidents.reported`: `IncidentReported {incidentId, service, severity, occurredAt}`, written through
  the **transactional outbox**, with key `incidentId`. The triage worker consumes it and is idempotent through
  the incident's status.
- Each topic has a `.DLT` dead-letter topic.

### LLM rules (every milestone)

- Every model call goes through the `TriageClassifier` **port** (milestone 2), or through the agent's single
  `ChatModel` loop (milestone 7). No controller touches `ChatClient` directly.
- Code consumes the output, so it is **structured output** (`.entity(TriageResult.class)`), and it is **validated
  in code** afterwards. The schema guarantees the shape, not the truth (AI-ENG-2).
- Incident text is **untrusted data**. It goes inside `<incident>` tags, and the system prompt says that tagged
  content is never instructions (AI-ENG-6).
- From milestone 6, **PII is redacted before any text leaves the process**: prompts, embeddings and MCP results.
- No database transaction is held open during a model call.
- Prompt and completion logging stay **off** (`spring.ai.chat.observations.log-prompt` and `log-completion` default
  to `false`). Log ids, token counts and outcomes, never incident text.

## Secrets

- The only secret is the Anthropic API key (and, in milestone 10, AWS credentials, which come from your AWS CLI
  profile and never touch the repo).
- The key is read **only from the environment**: `spring.ai.anthropic.api-key: ${ANTHROPIC_API_KEY:}`. No real value
  goes in any YAML file, test, script, commit, Docker image or Terraform file.
- Milestone 1 adds `.env` to `.gitignore` and commits a `.env.example` with empty values. Compose loads `.env` with
  `env_file`. Your IDE run configuration reads the same variable.
- Tests never need the key. They use a fake `TriageClassifier` or a fake `ChatModel`, and the test profile sets a
  dummy key value so auto-configuration can start. Only the eval run (milestone 6) and manual runs call the real
  model.
- Ollama needs no key. That makes it the default for long eval loops if you want zero spend.
- In AWS (milestone 10) the key lives in **AWS Secrets Manager**. Terraform creates the secret *container* only. You
  set the value in the console or with `--secret-string file://...` from a file outside the repo, so the value is
  never in Terraform state or in shell history. The instance role is allowed to read only that one secret.
- If a key ever lands in git, **rotate it first** in the Anthropic console, then clean the history. Deleting the file
  is not enough.

## Milestones (prerequisites are Academy display codes)

| # | Title | Builds on |
|---|---|---|
| 1 | Skeleton: incidents in PostgreSQL with Flyway, an ingest API and tests | SPRING-BOOT-8, AI-ENG-7 |
| 2 | LLM triage: classify and summarise with Spring AI, structured output, timeouts and a fallback | AI-BUILD-1, AI-ENG-2 |
| 3 | Similar incidents: embeddings in pgvector and retrieval (RAG) over past fixes | AI-BUILD-2, AI-ENG-3 |
| 4 | The dashboard: a triage queue, similar incidents and stats in the browser | AI-BUILD-3, SPRING-BOOT-6 |
| 5 | An MCP server: expose search and triage as tools and use them from an AI client | AI-BUILD-4, AI-ENG-4 |
| 6 | Evals and guardrails: a golden set, accuracy tracking and PII redaction | AI-BUILD-5, AI-ENG-6 |
| 7 | An agent: auto-triage with tool calls and a human approval step | AI-BUILD-6, AI-ENG-5 |
| 8 | Async and resilient: Kafka ingestion via the outbox, retries and a circuit breaker on the LLM | AI-BUILD-7, DIST-SYS-2, DIST-SYS-3 |
| 9 | Observability: latency, token and cost metrics, traces and a Grafana board | AI-BUILD-8, DIST-SYS-7 |
| 10 | Ship it: Docker, Terraform on AWS, README, demo and the interview story | AI-BUILD-9, DIST-SYS-11 |

## Design patterns used (see REFERENCE-2 for each)

| Pattern | Where | Milestone |
|---|---|---|
| Repository | `IncidentRepository`, `TriageRepository` | 1, 2 |
| Strategy | swappable `ChatModel` (Anthropic / Ollama), `EmbeddingModel`; `ActionHandler` per action type | 2, 3, 7 |
| Adapter (port and adapter) | `SpringAiTriageClassifier` adapts `ChatClient` to the `TriageClassifier` port | 2 |
| Template Method | `TriagePipeline.run`: prepare → classify → validate → record | 2 |
| Decorator | `TimeoutTriageClassifier`, then `ResilientTriageClassifier`, around the real classifier | 2, 8 |
| Facade | `IncidentMcpTools` over the incident, similarity and triage services | 5 |
| Chain of Responsibility | input guards (redaction, length limit) | 6 |
| Command + Registry | `PendingAction` executed by the `ActionHandler` registered for its type | 7 |
| Transactional Outbox, Idempotent Consumer | Kafka ingestion | 8 |
| Circuit Breaker, Retry, Timeout, Fallback | around the LLM | 8 |
| Observer | Micrometer observations and `@TransactionalEventListener` | 3, 9 |

Name the pattern only where it removes real complexity. An interviewer will ask "why not just an if-statement?"

## Every milestone file has these sections, in order

`## Where we are` · `## The problem` (a second-person story hook with stakes) · `## Goal` · `## What you build`
(a checklist) · `## Acceptance criteria` (concrete and checkable; test names match the real tests) · `## Steps`
(grouped into 2–3 sessions of 30–60 minutes, numbered, with the key Java 21 code copied from the verified project)
· `## Hints` · `## Stretch` · `## Talk about it in interviews` (4-layer answers: what, how it works inside, where used,
what goes wrong) · `## Done when` (a short checklist) · `## Commit` (message and tag, e.g. `git tag p3`) · `## Next`.
Refer to lessons by display code only.

Interactive minimums per milestone (as in `docs/LESSON-TEMPLATE.md`): 2+ **Predict:** moments, 3–4 quizzes (one
easy, one or two medium, one hard), and at least one diagram of what the milestone adds.
