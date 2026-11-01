## The goal

By **Mon 1 Feb 2027** you have learned backend from scratch to architect (all 110 Academy lessons) and
can walk into any SDE II / Senior Java backend loop: DSA coding, LLD, HLD / system design, and deep
Java and Spring Boot internals, answered at the depth an experienced interviewer expects.
Thirteen weeks plus a kickoff day: about 8½ hours a week of core, up to 11 with stretch.

## Every day has three levels

| Level | Weekday | What | When |
|---|---|---|---|
| 🟢 **Core** | 60 min | **Scratch lesson** 30 + **DSA** 30 | Normal days. The end date is built on core alone |
| ⭐ **Stretch** | +30 min | **Say it out loud**: an interview question on the day's topic, judged in 4 layers | Good days. Moves you ahead; skipping it never puts you behind |
| 🟡 **Floor** | 25–30 min | **Just the lesson** | Bad days. The scratch path never stops and the day still counts |

| Day | Core |
|---|---|
| **Saturday** (2 h) | **Two** scratch lessons 60 + **LLD** design 60 (timed DSA 30 is stretch) |
| **Sunday** (1 h 30) | Scratch lesson 30 + **HLD** case study 45 + **weekly review and catch-up** 15 (weeks 1–9: two lessons, 2 h); stretch: **Capstone** milestone 60 |

The Today page shows core first, stretch below it, and a **Short on time? Do just this** button for
the floor.

## The 7 rules (strict)

1. **Same time slot every day.** Pick it on the kickoff day (for example 7:00–8:00 before work) and
   protect it. Start the first core step within a minute of sitting down.
2. **Never a zero day.** On a terrible day, do the floor: just the lesson. That keeps the streak.
3. **No bingeing, no backlog.** Missed core goes into Sunday's catch-up slot; anything still left on
   Sunday night is dropped, not carried. Never add more than 30 extra minutes to a day.
4. **Recall before reading.** Attempt every problem for 22 minutes and answer every question out loud
   *before* opening the model answer. Reading first is the same as skipping.
5. **Every concept answer in 4 layers:** what it is → how it works inside → where you used it → what
   goes wrong. One layer is a junior answer; four layers is the depth interviewers want.
6. **Out loud, not in your head.** Interviews are spoken. Use Judge my answer (speak or Record).
7. **Every mock is scored and logged.** Four mocks with Claude (weeks 10 and 12, LLD and system
   design) plus two finals in week 13. Below 6/10 means that topic goes first the next week.

Stuck on a page? Use **Stuck?** (the Lifeline): nudge first, key idea next, full explanation last.
No new resources: everything is in this app.

## The 13-week map

The lessons run the Academy's *Zero to Pro* path in order (one a day, two on Saturdays); every lesson has a section code such as
JAVA-FUND-3 and a step number, and practice follows what they covered.

| Week | Dates | Scratch lessons | DSA pattern | LLD (Sat) | HLD (Sun) |
|---|---|---|---|---|---|
| 0 | Sun 1 Nov | Foundations (BE-STORY-1) + kickoff | — | — | — |
| 1 | 2 – 8 Nov | JDK/JRE/JVM, how Java runs, OOP, equals/hashCode, complexity, Java idioms, exceptions, generics | Arrays, hashing, two pointers | Parking lot | URL shortener |
| 2 | 9 – 15 Nov | Collections internals, JVM memory, hidden costs, streams, SOLID, threads, I/O and Jackson | Sliding window, prefix sums | Vending machine | Unique ID generator |
| 3 | 16 – 22 Nov | HTTP in depth, sockets and TCP, CGI → servlets → MVC, DI container, J2EE → Spring, IoC | Stacks, binary search | Singleton and factory | Image hosting |
| 4 | 23 – 29 Nov | Bean lifecycle, AOP, Boot internals and startup, annotations, MVC pipeline, REST API design | Linked lists, intervals | Logging framework | Idempotent API |
| 5 | 30 Nov – 6 Dec | SQL basics, data modeling and normalization, window functions, JDBC, JPA, indexes, isolation | Trees | Pluggable cache eviction | Ticket booking |
| 6 | 7 – 13 Dec | Query performance, SQL patterns, JPA lifecycle, testing, Java 8→25, class loading, GC, memory model | Heaps, top-K | Bounded blocking queue | Distributed cache |
| 7 | 14 – 20 Dec | Locks, atomics, executors, optimisation playbook, interview technique, design patterns | Graphs: BFS and DFS | Notification system | Chat application |
| 8 | 21 – 27 Dec | LLD method and cases, CompletableFuture, virtual threads, Spring Security, OAuth2, web security, Actuator | Topological sort, union-find, Dijkstra | Rate limiter | News feed |
| 9 | 28 Dec – 3 Jan | Debugging, building blocks, Redis, Kafka, scaling databases, NoSQL, design method and kit, SOA | Backtracking, tries | Elevator | Message queue |
| 10 | 4 – 10 Jan | Microservices, service communication, resilience, Linux and the OS, Docker/K8s, CI/CD, distributed theory | DP 1 | **Mock (LLD)** | **Mock (system design)** |
| 11 | 11 – 17 Jan | Event-driven, schedulers, case studies, betting platform, modular monoliths, reactive, GC diagnosis | DP 2, greedy | Undo/redo editor | Payment system |
| 12 | 18 – 24 Jan | Profiling, extending Spring, shipping a feature, AI engineering: LLMs, prompting, RAG, MCP, agents | Design data structures | **Mock (LLD)** | **Mock (system design)** |
| 13 | 25 – 31 Jan | Evals, Spring AI, AI-native, what's next (Mon–Thu) | Re-solves, timed | Final LLD mock | Final system design mock |

## The capstone (Sunday stretch)

One real Spring Boot service, **TicketHub** (event-ticket booking), built in your own repo over 12 Sundays:
plain-Java domain → in-memory engine → Boot skeleton → REST done properly → Postgres + JPA with seat locking
→ tests that prove no double booking → hold expiry → security → Redis → Kafka via outbox + Resilience4j →
Docker/Kubernetes → README, load test and the interview story. Each milestone (Academy CAPSTONE-1 … 12) only
uses what the week's lessons taught. It is stretch: skip a week and the plan is still on time; the next
milestone starts from the previous tag.

## When you fall behind

- **A missed day:** Sunday's catch-up slot takes the most important core session (DSA before lesson
  before design). Do not double up on Monday.
- **A whole week lost** (illness, release crunch): do floors only that week, skip its LLD and HLD, and
  restart on the following Monday's plan. Read the lessons you missed on later Saturdays' stretch time.
- **Two weekly reviews with gaps in a row:** stop stretch for a week and spend that time on the gaps.

## Done means

All 110 Academy lessons, about 70 DSA problems solved alone, 10 LLD designs and 10 HLD case studies,
6 mocks logged with the last three at 7/10 or better, and every weekday's concept said out loud on the
days you did stretch.
