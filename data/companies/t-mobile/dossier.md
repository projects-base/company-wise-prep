# T-Mobile (TMUS Global Solutions) — interview dossier

Target: **Senior Software Engineer – Resiliency** (recruiter title "Sr Engineer, Software"), TMUS
Global Solutions, Hyderabad, hybrid, 5–8 years. The application came through ANSR. **No interview
date yet.** Candidate: about 5.5 years as a Java/Spring Boot backend engineer. Researched 2026-10-08.

**Confidence.** Everything here is `claimed`. The JD came from the recruiter and is treated as the
official posting. A public copy of the same role is live on ANSR's Talent500 site as
[T500-28132](https://talent500.com/jobs/t-mobile/sr-engineer-software-hyderabad-T500-28132/)
(opened 2026-08-03).

**Source quality.** The postings are strong; the interview reports are thin.
- **Postings:** the TMUS India careers site runs on Talent500, and its public API lists 81 active
  Hyderabad postings. 10 relevant ones were read in full.
- **Interview reports:** Glassdoor and Fishbowl return 403, so reports were read only through
  search-engine snippets. LeetCode Discuss has nothing usable after 2021.
- **The Resiliency team's own loop:** no report describes it. The loop below comes from Hyderabad
  Java/backend reports.
- **The bank:** 8 sightings in total (4 new questions, 4 appended to existing ones).

## 1. TL;DR

- **The loop (reported, not official):** a virtual technical round 1 (core Java, Spring Boot, SQL,
  one coding problem, SOLID, some design), then round 2, a virtual **HLD of your current project**
  with a US interviewer. Round 3 is **face to face in Hyderabad**: HLD, a **Kafka scenario** and DSA.
  An HR round follows the same day. ANSR is TMUS's **exclusive recruiting partner**, so every
  message and offer comes through ANSR / 1Recruit.
- **The job is SRE-flavoured, but the interviews may not be.** The JD wants first-principles
  distributed systems, plus GitLab CI, Python/Bash/YAML, AWS, Kubernetes and Vault/CyberArk. The
  reported TMUS loops test Java backend fundamentals. Prepare both: the Java side is cheap for
  Akhil, and the resiliency side is what wins this role.
- **Over-prepare failure thinking.** The JD asks for "first-principles understanding of distributed
  systems, fault tolerance, and failure modes — not just framework familiarity". Be able to take any
  component, list how it fails, say how you would detect each failure, and describe how the system
  survives it. Use §6's primer.
- **Turn the Java background into resiliency stories.** Use timeouts, retries, circuit breakers,
  idempotent payments, Actuator health checks, incident RCAs and DB failover you have lived
  through. For the current-project HLD, draw your own system with its failure modes and recovery
  paths. Section 6 shows how.
- **Where people fail (inferred):** naming tools without mechanisms ("we use Kafka"); no numbers
  for RTO/RPO; treating failover as DNS only and forgetting the data; and no answer for
  split-brain. Reports also mention offers put "on hold" after HR, so keep other processes alive.

## 2. The loop

**What TMUS says.** Nothing about rounds. The India careers site
([talent500.com/t-mobile/careers/india](https://talent500.com/t-mobile/careers/india/)) and every
posting say TMUS Global Solutions "has engaged ANSR, Inc. ("ANSR") as its exclusive recruiting
partner ... communications regarding TMUS Global Solutions opportunities or employment offers will
be issued only through ANSR and the 1Recruit platform". They also say TMUS never asks for payment,
bank details or ID numbers before a formal offer is accepted. Applications carry multiple-choice
screening questions on years of experience; the Resiliency posting asks about "Python / Java".
careers.t-mobile.com has no hiring-process page for these roles, and its US Workday site returns
nothing for Hyderabad.

**What candidates report (claimed, snippets only):**

| # | Round | Format | What it assesses | Source |
|---|---|---|---|---|
| 0 | Application screening | Talent500 MCQ | Years in the core skill | [posting](https://talent500.com/jobs/t-mobile/sr-engineer-software-hyderabad-T500-28132/) |
| 1 | Technical | Virtual; for an Associate SE it was run by a third party | Core Java, Spring Boot, SQL queries, one coding problem, SOLID, system design. Senior SE Oct 2025: binary search, "optimising an API", "how does your day look"; the focus was on fundamentals and approach | [Assoc SE 2026](https://www.glassdoor.co.in/Interview/T-Mobile-Associate-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,36.htm), [Senior SE Oct 2025](https://www.glassdoor.co.in/Interview/T-Mobile-Senior-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,33.htm) |
| 2 | HLD of your current project | Virtual, interviewer from the US | Can you explain and defend your own system's architecture? | [TMUS community, undated](https://www.glassdoor.com/Community/tmus-global-solutions-india/i-have-r1-interview-for-senior-engineer-role-with-tmus-can-someone-help-with-their-interview-experience-thanks-in) |
| 3 | F2F in Hyderabad | In person | HLD, a Kafka scenario question, DSA | same thread; a 6 March (2026) F2F third round is [confirmed separately](https://www.glassdoor.co.in/Community/tmus-global-solutions-india/right-after-my-third-round-of-technical-interviews-which-was-f2f-in-hyderabad-on-march-6th-i-had-an-hr-round-they-gave-me-a) |
| 4 | HR | Same day as round 3 in one report | Compensation and documents. Offers were later put "on hold" in more than one report | same |

**Other reports:**
- A Fishbowl thread mentions "L1, L2, R3 face to face"
  ([offer thread](https://www.fishbowlapp.com/post/hi-i-got-an-offer-from-tmus-global-solutions-i-just-wanted-to-know-about-the-work-environment-and-the-office-location-in)).
- A Software Engineer report from Jan 2026 (location unclear): screening, then two backend
  technical rounds with the four OOP principles, a shortest-path graph algorithm, and simple
  code-fixing/logic tests
  ([Glassdoor SE](https://www.glassdoor.com/Interview/T-Mobile-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,26.htm)).
- Aug 2026, Hyderabad, full-stack contract role: resume and project questions, API security
  (JWT/OAuth), and easy-to-medium whiteboard coding
  ([Glassdoor Full Stack](https://www.glassdoor.co.in/Interview/T-Mobile-Full-Stack-Developer-Interview-Questions-EI_IE9302.0,8_KO9,29.htm)).

**ANSR.** Every posting confirms that ANSR is the exclusive recruiting partner. Light Reading
(Jun 2026) says TMUS Global Solutions "was established in October 2025 to employ the long-term
contractors and vendors" who already supported T-Mobile. No source says ANSR built or operates the
centre, so this dossier does not claim it. What it means for the process:
- Scheduling, offers and paperwork all go through ANSR / 1Recruit.
- Several Hyderabad reports say the candidate "applied through a staffing agency", or that a third
  party ran round 1.
- Reply only to ANSR channels and treat any other "TMUS offer" as fraud, as the postings instruct.

## 3. The bar

From the posting:
- "Strong first-principles understanding of distributed systems, fault tolerance, and failure modes."
- Ownership of complex features "end-to-end with minimal hand-holding".
- "Escalate with context and a proposed path forward".
- Documentation as "a first-class deliverable".
- Mentoring through code review.

The team is "async-first" across US and India time zones and holds "high standards for
reliability, documentation, and operational discipline".

What a hire probably looks like (inference):
- When asked "what happens if X dies", you answer in layers: detection (health check, timeout,
  missing heartbeat), decision (who decides, quorum, fencing), switch (traffic, then data), verify
  (synthetic transactions), and fail back.
- You give numbers: RTO/RPO, replication lag, DNS TTL, probe intervals, error budgets.
- You can write the automation: a GitLab pipeline that runs a failover job, pulls secrets from
  Vault, and calls a Python script that drains, switches and verifies.
- You write things down: the runbook, the diagram and the readiness dashboard are part of "done".

## 4. Question patterns

Merged from `data/_staging/t-mobile.json` (now in `data/_staging/merged/`): **8 sightings, 4 new
questions, 4 appended** to existing ones.

| Type | Questions with T-Mobile sightings |
|---|---|
| DSA | [`dsa/binary-search`](../../questions/dsa/binary-search.yaml) (Senior SE Hyd, Oct 2025, new), [`dsa/dijkstra-shortest-path`](../../questions/dsa/dijkstra-shortest-path.yaml) (SE, Jan 2026) |
| DOMAIN | [`domain/optimize-slow-api`](../../questions/domain/optimize-slow-api.yaml) (Senior SE Hyd, Oct 2025, new), [`domain/solid-principles`](../../questions/domain/solid-principles.yaml) (Assoc SE Hyd, 2026), [`domain/oauth-and-jwt`](../../questions/domain/oauth-and-jwt.yaml) (Full Stack Hyd, Aug 2026) |
| JAVA | [`java/polymorphism-and-static-method-overriding`](../../questions/java/polymorphism-and-static-method-overriding.yaml) (Assoc SE Hyd, 2026, new), [`java/oop-four-pillars`](../../questions/java/oop-four-pillars.yaml) (SE, Jan 2026, new) |
| BEHAVIORAL | [`behavioral/walk-through-current-work`](../../questions/behavioral/walk-through-current-work.yaml) ("how does your day look", Oct 2025) |

**Reported but not staged** (no date or no concrete question): "HLD of your current project"
(round 2) and a "Kafka scenario based question" (round 3), from the undated TMUS community thread.

**Already in the bank, relevant to this role** (sighted at other companies):
- [`behavioral/system-failure-and-fault-tolerance-story`](../../questions/behavioral/system-failure-and-fault-tolerance-story.yaml), the single most relevant one
- `hld/design-metrics-monitoring-system`, `hld/idempotency-in-payment-systems`, `hld/design-payment-system`
- `domain/kafka-architecture`, `domain/debugging-across-microservices`, `domain/cap-theorem`, `domain/dbms-replica-snapshot-checkpoint`
- `hld/design-distributed-key-value-store`, `hld/design-job-scheduler`, `hld/design-configuration-service`
- `behavioral/handling-ambiguity`, `behavioral/delivering-with-little-guidance`, `behavioral/how-you-use-ai-tools`

**Resiliency questions to expect** (inferred from the JD and the App-SRE postings' worked examples,
[T500-29769](https://talent500.com/jobs/t-mobile/senior-engineer-site-reliability-hyderabad-T500-29769/);
these are not reported questions):
1. Design automated failover of a payment API between two datacenters. What is your RTO/RPO?
2. A failover script ran but customers still hit the old DC. Why? (DNS TTL, client caching,
   connection pools, sticky sessions.)
3. How do you know a standby is actually ready to take traffic? Design the readiness dashboard.
4. An API is down: is it Kubernetes pod networking, the API gateway, or DB latency? Walk through it.
5. Pods stuck in Pending, or CrashLoopBackOff: debug them.
6. Write a GitLab pipeline that deploys, smoke-tests and promotes, with a manual failover job.
7. How do pipelines and pods get DB credentials without storing them in Git? (Vault/CyberArk.)
8. A Kafka consumer group is lagging after a DC switch, or messages are duplicated. What happened?
9. Define SLIs/SLOs for device activation, and when you would page someone.
10. Tell me about an incident you led: timeline, RCA, and the prevention that stuck.

## 5. Behavioural

T-Mobile's Un-carrier values
([careers](https://careers.t-mobile.com/culture-and-benefits)):
- **Love our customers**
- **One team, together**
- **Dream big and deliver** ("act like owners")
- **Do it the right way. Always.**
- **We won't stop**

TMUS Global Solutions adds "bold thinking, radical simplicity, and a relentless 24/7 delivery
mindset" ([India careers](https://talent500.com/t-mobile/careers/india/)). The JD lists self-driven,
first-principles, ambiguity-tolerant, relationship builder and knowledge sharer.

| Value / trait | Story to map (from Akhil's backend work) |
|---|---|
| Love our customers | A production failure that hurt users, and the fix that stopped it recurring (timeouts, idempotency, a retry storm tamed). Say what the customer felt. |
| Dream big and deliver / accountability | A feature or migration owned end to end with little guidance, where you escalated "with context and a proposed path". |
| One team, together / relationship builder | Unblocking work that depended on DBAs, network or another team; driving the conversation yourself. |
| Do it the right way | Refusing to ship without a rollback plan, a runbook, or tests on a critical path. |
| We won't stop / continuous improver | Automating a manual operational step (deploy, data fix, health check) after doing it twice. |
| Knowledge sharer | A runbook, design doc or onboarding guide you wrote that others used; mentoring through code review. |
| Comfortable with ambiguity | Starting with an incomplete spec, shipping something workable, iterating. |
| AI tools in the workflow | Concrete use of Claude/Copilot: scaffolding scripts, writing tests, reviewing YAML, and how you verify the output. |

Have one sentence ready on why T-Mobile: production-critical systems, real scale (payments and
activations), and a move from feature work to the reliability of the whole platform.

## 6. Company-specific angle: resiliency for payments and activations at telecom scale

### 6.1 Presenting Java backend experience as resiliency experience

Akhil has built and run production services. Reframe that work in the JD's terms; do not claim
SRE titles you did not hold.

| What you did as a backend engineer | How to say it for this role |
|---|---|
| Set timeouts, retries, circuit breakers (Resilience4j / Feign / WebClient) | "I designed for dependency failure: bounded timeouts, retry with backoff and jitter, circuit breakers so one slow service could not cascade." |
| Idempotency keys, outbox, exactly-once-ish processing | "I made writes safe to retry, which is what makes automated failover safe: a replayed request does not double-charge." |
| Spring Boot Actuator health and readiness, Micrometer metrics | "I built health checks that reflect real readiness (DB, downstream, queue), and exposed metrics that dashboards and alerts consumed." |
| Production incidents, on-call, RCAs | "I led or took part in incident response: detection, mitigation, root cause, and the follow-up automation." |
| DB connection pools, replicas, migrations | "I know what breaks at the data layer during a switch: pool exhaustion, replica lag, stale connections, long transactions." |
| CI/CD pipelines you touched (Jenkins/GitHub Actions/GitLab) | "I have shipped through pipelines and can own them as code; GitLab CI YAML is the same model." |
| Kafka/RabbitMQ consumers | "I understand consumer offsets, rebalancing and duplicate delivery, which all matter when a DC fails over." |

Rule: every story ends with **what you changed so it could not fail the same way again**.

### 6.2 Resiliency-engineering primer (what to be able to discuss)

**1. Failure modes and fault tolerance from first principles.**
- **What fails:**
  - processes crash or hang;
  - nodes die or go slow ("gray failure" is the worst);
  - networks partition, drop or delay packets;
  - disks fill;
  - clocks skew;
  - dependencies time out;
  - config or deploys break things;
  - certificates expire;
  - capacity runs out;
  - retries amplify load (retry storms);
  - humans run the wrong command.
- **Tolerance tools:** redundancy, isolation (bulkheads, cells), timeouts on everything, retries
  only when idempotent (with backoff and jitter), circuit breakers, load shedding and back-pressure,
  graceful degradation, and fencing.
- **Key insight:** you cannot tell a slow node from a dead one, so every failover decision is a
  timeout plus a guess. Design for the wrong guess.

**2. Active-active vs active-passive.**
- **Active-passive:** one site serves traffic and a standby is replicated (hot, warm or cold).
  It is simpler and avoids write conflicts. The costs are idle capacity, a failover step that must
  be tested, and possible data loss with async replication.
- **Active-active:** both sites serve traffic. You get capacity and instant absorption, but writes
  need partitioning (home region per customer), conflict resolution, or a globally consistent store.
  Each site must keep headroom to take the other's load (run at ≤50% if N=2).
- For payments, a common choice is active-active reads with a single writer per customer or
  partition, which avoids double-spend.

**3. RTO / RPO.**
- **RTO** is how long you can be down. **RPO** is how much data you can lose, measured in time.
- Sync replication gives RPO ≈ 0 but adds write latency and couples availability to the remote
  site. Async replication gives RPO = replication lag.
- AWS's DR ladder: backup/restore → pilot light → warm standby → multi-site active-active, each
  with lower RTO/RPO at higher cost
  ([AWS DR options](https://docs.aws.amazon.com/whitepapers/latest/disaster-recovery-workloads-on-aws/disaster-recovery-options-in-the-cloud.html)).
- Know which tier a payments ledger needs (RPO ≈ 0) and which tier a notifications service needs
  (minutes are fine).

**4. Cross-datacenter failover.**
- **Traffic:**
  - DNS failover or GSLB, driven by health checks
    ([Route 53 failover](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/dns-failover.html)).
    TTLs bound how fast clients move, but resolvers and JVMs cache DNS (`networkaddress.cache.ttl`).
  - Anycast or global load balancers move traffic faster.
  - Long-lived connections (pools, gRPC, Kafka) must be drained or recycled.
- **Data:**
  - Promote the replica and fence the old primary (STONITH, revoke its write lease) so two
    writers never coexist.
  - Check replication lag before promoting, and decide what to do with un-replicated writes.
- **Split-brain:**
  - Both sides believe they are primary after a partition. Prevent it with quorum (an odd number of
    voters, or a witness in a third site), leases with fencing tokens, and never letting the minority
    side accept writes.
- **Orchestration order:** freeze changes → check readiness → stop writes at the source (or
  fence) → wait for replication to catch up (planned failover) → promote → switch traffic → run
  synthetic transactions → monitor → plan the failback.
- **Planned and unplanned failovers differ:** a planned one can drain and reach RPO = 0; an
  unplanned one usually cannot.

**5. Health checks and readiness.**
- **Kubernetes probe types:**
  - *Liveness*: restart me if I am stuck.
  - *Readiness*: send me traffic only if I can serve it.
  - *Startup*: give me time to boot.

  ([K8s probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/))
- **Shallow vs deep:**
  - Shallow checks (is the process up?) are cheap but miss dependency failures.
  - Deep checks (can I reach the DB?) can cascade: if the DB is slow, every pod goes unready and
    nothing serves.
  - Fail open when all instances fail at once
    ([AWS Builders' Library](https://aws.amazon.com/builders-library/implementing-health-checks/)).
- **Failover readiness is more than "is it up":**
  - Is the standby's replication lag under the RPO?
  - Are capacity, config, secrets and certificates in sync?
  - Did the last drill pass?

  Put these on a dashboard. That is exactly the JD's "real-time visibility into failover readiness".

**6. Chaos engineering.**
- **The method:** define the steady state (SLIs), form a hypothesis, inject a real-world fault
  (kill a pod, add latency, partition a DC, fail DNS), keep the blast radius small with abort
  criteria, automate it and run it continuously
  ([principlesofchaos.org](https://principlesofchaos.org/)).
- Game days and regular failover drills are the telecom-friendly version. An untested failover
  path is a hypothesis, not a capability.

**7. SLI / SLO / error budgets.**
- **SLI:** a measured ratio, e.g. good activations / all activations, or p99 latency under X.
- **SLO:** the target, e.g. 99.95% over 28 days. The **error budget** (1 − SLO) is the room you
  have for change and failure.
- Alert on fast and slow **burn rates**, not on CPU
  ([SRE book](https://sre.google/sre-book/service-level-objectives/),
  [workbook](https://sre.google/workbook/implementing-slos/),
  [Prometheus alerting practices](https://prometheus.io/docs/practices/alerting/)).
- An automated-failover trigger is essentially an SLI breach plus a readiness check on the other
  side.

**8. Observability: metrics, logs, traces, OTEL.**
- **The three signals:**
  - *Metrics* answer "is something wrong?" (RED/USE, Prometheus, Grafana).
  - *Logs* answer "what happened?" (Splunk).
  - *Traces* answer "where?" (OpenTelemetry → a backend).
- OpenTelemetry standardises collection, with the SDK or Java agent, the Collector, and OTLP
  ([OTel signals](https://opentelemetry.io/docs/concepts/signals/)).
- An "observability pipeline" means collectors that receive, enrich, sample, route and drop
  telemetry before it reaches Splunk or Grafana, which controls cost and keeps signals useful.
- **AIOps:** anomaly detection, event correlation and auto-remediation on top of this data. Several
  TMUS postings mention AIOps and ChatOps.

**9. Runbook automation.**
- Turn the runbook into code:
  - idempotent steps;
  - preconditions and postconditions checked automatically;
  - dry-run mode;
  - a manual approval gate for risky steps;
  - logs to a ticket or chat;
  - a rollback path.
- Typical toolchain: GitLab CI manual jobs or schedules → Python/Bash or Ansible playbooks →
  secrets from Vault/CyberArk at runtime, never in YAML → results posted to Splunk or Slack.
- The App-SRE posting's example: "Update a runbook to add automated Kubernetes log collection
  instead of manual steps."

**10. Payments and activations at telecom scale.**
- **Payments** must never be lost or doubled:
  - idempotency keys end to end;
  - a ledger as the source of truth;
  - RPO ≈ 0 for the writer;
  - reconciliation jobs after any failover;
  - payment-gateway timeouts get "unknown" handling (query the status, never blindly retry the
    charge).
- **Device activations** are multi-step workflows across many systems: billing, the network
  (HLR/HSS/UDM provisioning), the SIM/eSIM platform and CRM. Treat them as a saga with retries
  and compensations. A failover mid-activation must resume rather than restart, which needs
  persisted workflow state and idempotent steps.
- **Peaks:** device launches, promotions and month-end billing. Capacity in the surviving DC must
  absorb them.
- **Blast radius:** customers "feel it immediately" (JD). Prefer partial degradation, e.g. accept
  the activation and complete it asynchronously, over a hard outage.
- The telecom specifics (HLR/HSS/UDM, eSIM) are general domain knowledge, not something TMUS has
  published about its systems. Use them as vocabulary, not as claims about T-Mobile internals.

### 6.3 Akhil's likely gaps and the fastest bridge

| Gap | Why it matters | Fastest bridge (evenings) |
|---|---|---|
| **Python / Bash** | JD must-have; 11/11 postings | Rewrite one Java utility as a Python script (argparse, requests, retries, exit codes, logging). Bash: `set -euo pipefail`, `trap`, `curl` + `jq`, loops over `kubectl` output. |
| **GitLab CI YAML** | JD must-have | Build a `.gitlab-ci.yml` for a Spring Boot repo: stages, `rules`, `needs`, artifacts, caches, `include`/`extends`, environments, a `when: manual` failover job ([quick start](https://docs.gitlab.com/ci/quick_start/), [YAML reference](https://docs.gitlab.com/ci/yaml/)). |
| **Kubernetes at production scale** | JD must-have | On kind/minikube: Deployment, Service, Ingress, probes, HPA, PodDisruptionBudget, rolling update + rollback. Practise `kubectl describe/logs/events/top` on broken pods ([basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/)). |
| **Vault / CyberArk** | JD must-have | Vault dev server: KV secret, dynamic DB credentials, leases, a GitLab job authenticating with JWT/OIDC, the Vault Agent injector on K8s ([what is Vault](https://developer.hashicorp.com/vault/docs/what-is-vault), [GitLab + Vault](https://docs.gitlab.com/ci/secrets/hashicorp_vault/), [Vault on K8s](https://developer.hashicorp.com/vault/docs/platform/k8s)). For CyberArk, know the concepts (a privileged-access vault, credential rotation, Conjur for app secrets); no hands-on is needed. |
| **Ansible** | Nice-to-have; 7/11 postings | One playbook: inventory, a role, a handler, `--check` mode; drain a node and restart a service ([ansible](https://github.com/ansible/ansible)). |
| **Observability tooling** | Nice-to-have; 7–9/11 postings | OTel Java agent on a Spring Boot app → Prometheus → Grafana panel for p99 and error rate; one burn-rate alert ([Grafana + Prometheus](https://grafana.com/docs/grafana/latest/getting-started/get-started-grafana-prometheus/)). For Splunk, know SPL basics (`index= ... | stats count by status`). |
| **AWS at production scale** | JD must-have | Know Route 53 health checks and failover, multi-AZ vs multi-region, RDS/Aurora replicas, EKS, IAM roles for service accounts. Terraform's AWS tutorial covers the basics ([Terraform AWS](https://developer.hashicorp.com/terraform/tutorials/aws-get-started)). |

**Be honest in the room.** "I have used X lightly and built Y to learn it" beats bluffing. The JD
explicitly values a "fast learner" who ramps "with minimal guidance".

## 6b. What the job postings ask for

11 entries: the recruiter JD, its public copy (T500-28132), and 9 more live Hyderabad postings on
Talent500 (SRE ×4, AI-platform backend, Java commissions, AIOps, Java full stack, systems
architecture), opened Sep 2025 to Oct 2026. Counts are keyword matches; the JD and T500-28132 are
the same role, so its skills count twice.

| Skill | Postings | What it implies for prep |
|---|---|---|
| python, bash-shell | 11, 9 | Live scripting is plausible; be fluent, not just familiar |
| observability (splunk, grafana, prometheus, opentelemetry) | 11 (9, 9, 7, 7) | Dashboards, alert rules, tracing; tie them to failover readiness |
| kubernetes | 10 | Probes, rollouts, HPA, debugging pods |
| ci-cd (gitlab-ci 6, jenkins 6, gitops 5) | 10 | GitLab YAML by hand; pipeline as the runbook executor |
| aws (azure 8, gcp 6) | 9 | Multi-AZ/region, Route 53, EKS, RDS failover |
| mentoring, documentation | 9, 9 | Runbook and design-doc stories; code-review mentoring |
| resiliency, incident-management | 8, 7 | Incident command, RCA, self-healing |
| ansible, terraform, iac | 7, 7, 6 | One playbook, one Terraform module |
| java, microservices, spring-boot | 6, 6, 3 | Akhil's strength; shows TMUS accepts Java for platform work |
| distributed-systems, fault-tolerance, failover-dr | 6, 3, 3 | The JD's core; see the §6 primer |
| aiops, ai-assisted-dev | 5, 4 | One honest AI-workflow story |
| secrets-management, yaml | 2, 2 | Only the Resiliency role, but they are must-haves there |
| slo-error-budgets | 3 | SLOs for payments and activations |

Posting bullets are requirements, not interview questions.

## 6c. Tech stack and frameworks to prepare

Full list with sources: `tech_stack` and `frameworks_to_prepare` in `company.yaml`. The profile
file (`data/profile.yaml`) did not exist yet, so the gap analysis assumes a Java 21 / Spring Boot /
JPA / REST / PostgreSQL backend engineer with about 5.5 years of experience.

**Frameworks & languages**

| Item | Category | Importance | Evidence |
|---|---|---|---|
| Python | scripting | core | JD must-have; 11/11 |
| Bash / shell | scripting | core | JD must-have; 9/11 |
| YAML | scripting | core | JD "strong YAML skills" |
| Kubernetes (EKS/AKS, some OpenShift) | infra | core | JD must-have; 10/11 |
| AWS | cloud | core | JD must-have; 9/11 |
| Azure | cloud | common | 8/11 as an alternative |
| Java / Spring Boot | language / framework | common | 6/11 and 3/11; Resiliency posting tags "Python or Java" |
| Go | language | common | 7/11 as an alternative |
| SQL/NoSQL (Oracle, RDS) | datastore | common | 4/11; reported SQL round |
| Kafka / RabbitMQ | messaging | common | 3/11; reported Kafka scenario |
| AI coding tools (Claude, Copilot, ChatGPT) | ai-tools | common | JD nice-to-have; required in the Java posting |

**Patterns**

| Item | Category | Importance | Evidence |
|---|---|---|---|
| Active-active / active-passive cross-DC failover | architecture-pattern | core | JD "automated failover, cross-datacenter orchestration" |
| Health checks / failover readiness | architecture-pattern | core | JD "health-check mechanisms ... failover readiness" |
| DR tiers, RTO/RPO | architecture-pattern | core | JD + SRE posting "backup, disaster recovery" |
| Runbook automation / self-healing | architecture-pattern | core | JD runbooks; App SRE self-healing |
| DNS / GSLB failover, load balancing | architecture-pattern | common | SRE posting: DNS, LB, traffic management |
| Circuit breaker, retry + backoff, timeouts, bulkhead, idempotency | architecture-pattern | common | JD fault tolerance; Java posting names them |
| SLI/SLO/error budgets | architecture-pattern | common | AI-platform posting |
| Incident management / RCA | architecture-pattern | common | 7/11 |
| Event-driven microservices | architecture-pattern | common | 6/11 |
| IaC / GitOps | architecture-pattern | common | 6/11, 5/11 |
| Smoke-test gates, blue-green/canary | architecture-pattern | mentioned | App SRE "smoke tests before promoting" |

**Tools**

| Item | Category | Importance | Evidence |
|---|---|---|---|
| GitLab CI/CD | ci-cd | core | JD must-have; 6/11 |
| HashiCorp Vault / CyberArk | security | core | JD must-have |
| Splunk, Grafana | observability | core | JD nice-to-have; 9/11 each |
| Prometheus, OpenTelemetry | observability | common | JD nice-to-have; 7/11 each |
| Ansible | tool | common | JD nice-to-have; 7/11 |
| Terraform | tool | common | 7/11; tag on T500-28132 |
| Jenkins | ci-cd | common | 6/11 |
| AppDynamics / Datadog / Dynatrace | observability | common | 5, 5, 3 /11 |
| ServiceNow / Jira / Confluence | tool | mentioned | SRE posting (ITSM) |

**Revise** (Akhil has these; refresh them for the interview), most important first:
1. **Distributed-systems failure modes** (F2, F1): one page listing each failure mode with one way
   to detect it and one way to mitigate it.
2. **Resilience patterns** (S8, F2): explain each from your own Resilience4j configs and the
   failure it prevented.
3. **Replication and data failover** (Q8, F2, Q6): Postgres streaming replication, RPO on promote,
   fencing.
4. **Actuator health/readiness and metrics** (D8): map them to K8s probes and Prometheus.
5. **Event-driven / Kafka scenarios and the outbox pattern** (S9, S8): consumer lag, duplicate
   delivery, rebalances after a DC switch.
6. **Core Java, OOP, concurrency** (A1, B2, B4, E1): the reported round-1 topics.
7. **GoF patterns for automation code** (E2–E4): Strategy per app type, a Template Method for
   drain → switch → verify, an Observer for health events.
8. **System design method + the monitoring case study** (F3, F6): design a "failover-readiness
   dashboard".
9. **SQL and slow-query diagnosis** (Q5, Q7): EXPLAIN, pg_stat_statements, and one latency story.

**Learn** (new or weak for him), most important first:
1. **Cross-DC failover, RTO/RPO, split-brain** (F2, Q8): read the AWS DR whitepaper and the Route 53
   failover guide; draw both topologies for a payment API.
2. **GitLab CI/CD + YAML:** in one evening, build a pipeline with rules, needs, includes and a
   manual failover job.
3. **Python + Bash ops scripting:** a health-check poller in Python and a strict-mode Bash wrapper.
4. **Kubernetes in production** (S9): break and fix the probes, rollouts and HPA on kind.
5. **Vault (CyberArk concepts):** dev server, dynamic DB credentials, a GitLab JWT login, the K8s
   injector.
6. **SLOs and burn-rate alerts** (F6): write SLOs for payments and activations.
7. **OTel → Prometheus → Grafana** (F6, D8): instrument a Spring Boot app end to end.
8. **Health-check design** (F6): shallow vs deep checks, fail-open behaviour, readiness vs failover
   gates.
9. **Chaos engineering / game days:** write one experiment plan.
10. **Ansible:** one playbook with a role and a handler.
11. **Terraform:** the AWS get-started tutorial; state, locking, modules.
12. **Blue-green / canary + smoke-test gates** (S9): add a canary stage to your pipeline.

## 7. Strategy

There is no date yet, and the recruiter has only asked for a resume. Budget about 25 min on
weekdays (more at weekends if available).

**Now, before a date is set:**
- Send the resume through ANSR. Rewrite 3–4 bullets in resiliency language (§6.1): timeouts and
  circuit breakers, idempotent payments, health checks and metrics, incidents and RCAs, pipeline
  ownership. Name GitLab, Kubernetes, AWS, Python or Vault only where it is true.
- Start the GitLab CI + Python mini-project from §6.3. It gives you something real to say for the
  must-haves.

**First 7 days after the date is set:**

| Day | Focus |
|---|---|
| 1 | Read §6.2 items 1–4. Draw active-passive and active-active for "payment authorisation" with RTO/RPO, DNS TTL and the fencing step. |
| 2 | Kubernetes: probes, rollouts, HPA, PDB on kind. Practise debugging Pending and CrashLoopBackOff. Map Actuator liveness/readiness to probes. |
| 3 | GitLab CI: finish the pipeline (build, test, deploy, smoke test, a manual failover job). Pull one secret from Vault in a job. |
| 4 | Python + Bash: a failover-readiness checker (replication lag, health endpoints, cert expiry) that exits non-zero and prints a report. Commit it. |
| 5 | Observability: OTel agent → Prometheus → Grafana; write SLOs and one burn-rate alert for activations. Rehearse question 3 from §4 ("is the standby ready?"). |
| 6 | Reported Java loop: OOP pillars, overriding vs hiding, SOLID, binary search, Dijkstra, "optimise a slow API", and a Kafka scenario (lag, duplicates, rebalance). |
| 7 | Mock HLD of **your current project**, 30 min out loud. Include failure modes per component, health checks, and what happens if one DC dies. Then rehearse 3 behavioural stories (§5). |

**After day 7 and before the interview:** alternate one resiliency scenario from §4's list of 10
with one DSA medium per day. Keep the last 2 days for review only.

**In the room:**
- For any component, answer "how does it fail, how do we know, how do we survive, how do we prove
  it".
- Give numbers.
- Say what you would write down: the runbook and the dashboard.
- If a tool is new to you, say how you would ramp on it and connect it to something you have run.

## 8. Sources

All accessed 2026-10-08.

**Job description**
- Recruiter email (ANSR), 2026-10-08: the job description, treated as the official posting.

**TMUS India careers and postings**
- [TMUS India careers (Talent500)](https://talent500.com/t-mobile/careers/india/); Talent500 public
  jobs API `prod-warmachine.talent500.co/api/jobs/?company_slug=t-mobile` (81 active postings).
- Postings:
  [T500-28132 Resiliency](https://talent500.com/jobs/t-mobile/sr-engineer-software-hyderabad-T500-28132/),
  [T500-29902 Sr SRE](https://talent500.com/jobs/t-mobile/sr-engineer-site-reliability-hyderabad-T500-29902/),
  [T500-29769 Sr App SRE](https://talent500.com/jobs/t-mobile/senior-engineer-site-reliability-hyderabad-T500-29769/),
  [T500-27010 Sr SRE](https://talent500.com/jobs/t-mobile/senior-engineer-site-reliability-hyderabad-T500-27010/),
  [T500-29903 SRE](https://talent500.com/jobs/t-mobile/engineer-site-reliability-hyderabad-T500-29903/),
  [T500-28888 Backend AI platform](https://talent500.com/jobs/t-mobile/sr-engineer-software-backend-hyderabad-T500-28888/),
  [T500-28390 Java](https://talent500.com/jobs/t-mobile/sr-engineer-software-java-hyderabad-T500-28390/),
  [T500-20351 AIOps](https://talent500.com/jobs/t-mobile/sr-engineer-software-aiops-hyderabad-T500-20351/),
  [T500-27093 Full stack](https://talent500.com/jobs/t-mobile/sr-engineer-software-java-hyderabad-T500-27093/),
  [T500-29934 Systems architecture](https://talent500.com/jobs/t-mobile/principal-engineer-systems-architecture-hyderabad-T500-29934/);
  [aggregator copy of T500-27016 (now 404)](https://jobs.joindevops.com/jobs/508869278-engineer-software-devops-automation-t500-27016).

**T-Mobile US careers**
- [Culture & values](https://careers.t-mobile.com/culture-and-benefits),
  [internship FAQ](https://careers.t-mobile.com/internship), and the
  [US Workday](https://tmobile.wd1.myworkdayjobs.com/en-US/External) (no India jobs).

**News and ANSR**
- [Light Reading, 9 Jun 2026](https://lightreading.com/operations/t-mobile-expands-tech-development-hub-in-india),
  [Outsource Accelerator, Jun 2026](https://news.outsourceaccelerator.com/t-mobile-hyderabad-gcc/),
  [UNI](https://www.uniindia.com/news/business-economy/tech-tmus-global-centre/3865600.html),
  [Deccan Chronicle](https://www.deccanchronicle.com/southern-states/telangana/t-mobile-us-setting-up-global-tech-hub-in-hyderabad-to-add-300-jobs-by-january-1913009),
  [ANSR Top GCC Jobs](https://ansr.com/careers/top-gcc-jobs-careers/).

**Interview reports (snippets only; pages return 403)**
- Glassdoor:
  [Senior SE](https://www.glassdoor.co.in/Interview/T-Mobile-Senior-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,33.htm),
  [Associate SE](https://www.glassdoor.co.in/Interview/T-Mobile-Associate-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,36.htm),
  [Full Stack](https://www.glassdoor.co.in/Interview/T-Mobile-Full-Stack-Developer-Interview-Questions-EI_IE9302.0,8_KO9,29.htm),
  [Software Engineer](https://www.glassdoor.com/Interview/T-Mobile-Software-Engineer-Interview-Questions-EI_IE9302.0,8_KO9,26.htm),
  [Senior Software Developer](https://www.glassdoor.com/Interview/T-Mobile-Senior-Software-Developer-Interview-Questions-EI_IE9302.0,8_KO9,34.htm).
- TMUS community threads:
  [R1 Senior Engineer](https://www.glassdoor.com/Community/tmus-global-solutions-india/i-have-r1-interview-for-senior-engineer-role-with-tmus-can-someone-help-with-their-interview-experience-thanks-in),
  [F2F March 6](https://www.glassdoor.co.in/Community/tmus-global-solutions-india/right-after-my-third-round-of-technical-interviews-which-was-f2f-in-hyderabad-on-march-6th-i-had-an-hr-round-they-gave-me-a).
- Fishbowl:
  [offer thread](https://www.fishbowlapp.com/post/hi-i-got-an-offer-from-tmus-global-solutions-i-just-wanted-to-know-about-the-work-environment-and-the-office-location-in),
  [2nd round thread](https://www.fishbowlapp.com/post/hi-everyone-i-will-have-the-t-mobile-2nd-round-of-interview-for-engineer-software-position-can-anyone-share-their-experiences-that).
- LeetCode Discuss (GraphQL): [1591871](https://leetcode.com/discuss/post/1591871/) (US, 2021),
  [7374212](https://leetcode.com/discuss/post/7374212/) (no content).

**Primer references**
- [AWS DR options](https://docs.aws.amazon.com/whitepapers/latest/disaster-recovery-workloads-on-aws/disaster-recovery-options-in-the-cloud.html),
  [Route 53 DNS failover](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/dns-failover.html),
  [AWS health checks](https://aws.amazon.com/builders-library/implementing-health-checks/),
  [AWS timeouts/retries/backoff](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/).
- [K8s probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/),
  [SRE book SLOs](https://sre.google/sre-book/service-level-objectives/),
  [SRE workbook](https://sre.google/workbook/implementing-slos/),
  [Prometheus alerting](https://prometheus.io/docs/practices/alerting/),
  [OTel signals](https://opentelemetry.io/docs/concepts/signals/),
  [Principles of Chaos](https://principlesofchaos.org/).
- [GitLab CI YAML](https://docs.gitlab.com/ci/yaml/),
  [Vault](https://developer.hashicorp.com/vault/docs/what-is-vault).
