# Tower Research Capital — interview dossier

Target: Software Engineer / Senior Software Engineer, Gurugram (core engineering / infra / trading
systems). Candidate: about 7 years of Java/Spring Boot backend. Researched 2026-10-07. Everything here is
`claimed` (web research) until Akhil verifies it.

**Source quality warning.** Tower publishes almost nothing about its interview process. India
reports are mostly LeetCode Discuss posts from 2020–2022, plus a few from 2024–2026 (one Java
backend SDE-1, one intern, three OA problem dumps). I found **no 2023–2026 report for a Senior
SWE in Gurugram**. Code360, Glassdoor, 1point3acres and Reddit blocked fetching. The 2026 "guides" (techinterview.org, techprep, quantt,
tradermath, quantvault) cite no primary sources and describe a US Super Day format, so treat
them as weak signals.

## 1. TL;DR

- **Loop (India, reported):** a HackerRank OA, then 2–3 elimination interviews and an HR or
  techno-managerial round. The OA format varies a lot: 2 coding + CS-theory MCQs in 135 min
  ([2025](https://leetcode.com/discuss/post/6375258/)), 7 *fix-the-bug* C++ tasks + 4 essay-style
  systems questions in 90 min ([Senior SWE, 2022](https://leetcode.com/discuss/post/1764282/)), or
  2026 new-grad OAs with Codeforces-hard tree/number-theory problems.
- **What separates them from a FAANG loop:** the CS-fundamentals depth. Expect TCP vs UDP,
  multicast reliability, virtual memory, CPU cache and TLB, RDTSC, C++ vtables and move semantics.
  These come up *alongside* DSA, and even the Java post-trade loop had OS/DBMS/networking theory.
- **Where people fail:** pace and depth. Interviewers say "implement it *quickly*" and keep adding
  follow-ups ([2021](https://leetcode.com/discuss/post/1689730/)). One C++ candidate passed DSA
  but failed on the C++ object model and the HFT LLD ([2021](https://leetcode.com/discuss/post/1702584/)).
  A Java candidate cleared DSA but was cut after a 45-minute rapid-fire round on Java internals
  ([2025](https://leetcode.com/discuss/post/6375258/)).
- **Over-prepare:** grid BFS/DFS (Shortest Bridge appears 3 times, Max Area of Island plus a hard
  follow-up), concurrency primitives (bounded blocking queue, lock-free stack, worker pool),
  networking (TCP/UDP/multicast/congestion control), memory hierarchy, and an order-book LLD.
- **Fit risk:** none of the current Gurugram postings asks for Java (0 of 7). Aim for
  post-trade, shared-services or platform teams. Show systems depth, not Spring Boot.

## 2. The loop

Tower does not publish its rounds. Its careers page only says the process is "straightforward"
with "no gotcha questions, random logic problems, or abstract, overly academic discussions"
([careers](https://tower-research.com/careers/)). Candidate reports partly contradict this, since
they describe timed OAs, theory MCQs and older puzzle rounds.

| # | Round | Format / time | What it assesses | Evidence |
|---|-------|---------------|------------------|----------|
| 0 | Online assessment (HackerRank) | 60–135 min; varies by role | **SWE-2 (2021):** 3 tricky coding + 1 MCQ in 75 min ([1537141](https://leetcode.com/discuss/post/1537141/)). **SDE-2 C++ (2021):** 7 MCQ + 2 debug + 2 coding, one LC-hard ([1702584](https://leetcode.com/discuss/post/1702584/)). **Senior SWE (2022):** 7 bug-fix + 4 subjective systems Qs, 90 min ([1764282](https://leetcode.com/discuss/post/1764282/), [2717817](https://leetcode.com/discuss/post/2717817/)). **Java SDE-1 (2025):** Java inheritance + medium DSA + OS/DBMS/network theory, 135 min ([6375258](https://leetcode.com/discuss/post/6375258/)). **Dev-tools 9 YOE (2021):** parsing, matrix, debug a tree, debug a C binary with strace ([1689730](https://leetcode.com/discuss/post/1689730/)) | 2021–2026 |
| 1 | Technical 1 | 45–75 min | DSA (often grid/graph) plus CS fundamentals or language. Examples: Shortest Bridge; Max Area of Island with a row/column-fill follow-up; LFU Cache with C++ smart pointers; TCP/UDP for 45 min, then a trie problem; Roman to Integer + Kth Smallest in BST after a resume screen | 2020–2025 |
| 2 | Technical 2 | 30–60 min | Language internals + LLD/concurrency. C++ track: vtables, then an HFT LLD (observers/callbacks). Java track: rapid-fire Java/JDBC/memory. Experienced: Task/wait scheduler → 3-worker pool under time pressure | 2021–2025 |
| 3 | LLD / design (some loops) | ~60 min | Multi-threaded buffered queue; lock-free stack using only AtomicReference; idempotency in payments ([1867832](https://leetcode.com/discuss/post/1867832/)). Older: design LinkedIn jobs ([988571](https://leetcode.com/discuss/post/988571/)) | 2020–2022 |
| 4 | Techno-managerial / HR | 30–60 min | Current work, why change, "build your project without public cloud", RDBMS depth; HR: "Why Tower?", offers in hand | 2022, 2025 |

Third-party guides say: OA → 1–2 sixty-minute phone screens → a Super Day of 4–6 rounds across
2–3 trading teams → team match ([techinterview.org](https://www.techinterview.org/post/3233476792/how-tower-research-capital-interviews-engineers/),
[techprep](https://www.techprep.app/blog/tower-research-interview-process)). No India report
describes a Super Day.

## 3. The bar

- **All rounds eliminate**, and some loops end after round 2 without explanation. Candidates
  report getting no feedback and sometimes being ghosted ([1867832](https://leetcode.com/discuss/post/1867832/)).
- **Speed counts on top of correctness.** Interviewers deliberately push pace ("we have to grill
  the candidates … we are dealing with finance") ([1689730](https://leetcode.com/discuss/post/1689730/)).
- **Depth under the abstraction.** For C++ seats they want a "very deep granular level
  understanding" of classes and vtables ([1702584](https://leetcode.com/discuss/post/1702584/)).
  For the Java seat, the questions went down to `int a = 10`, the String pool, and HashMap and
  connection-pool internals ([6375258](https://leetcode.com/discuss/post/6375258/)).
- **Senior level (inferred).** Of the two SSE OAs, one was mostly subjective systems questions
  (virtual-memory aliasing, reliable multicast, RDTSC, the CPU load path)
  ([1764282](https://leetcode.com/discuss/post/1764282/)). So the bar for a senior hire is a
  systems engineer who can reason about hardware and network behaviour, not just ship services.
- Compensation context: Senior SWE Gurugram offers were reported at ₹75 L–1.2 Cr total in 2021
  ([1341458](https://leetcode.com/discuss/post/1341458/), [1583793](https://leetcode.com/discuss/post/1583793/)).
  A 2025 comment calls the bar "higher difficulty than any of FAANG" ([7447700](https://leetcode.com/discuss/post/7447700/)).

## 4. Question patterns

These are staged in `data/_staging/tower-research.json` (82 unique questions, 88 sightings). After
the merge they will be at `data/questions/<type>/<slug>.yaml`.

**Grid / graph BFS-DFS: the most repeated theme**
- `data/questions/dsa/shortest-bridge.yaml`. Seen 3 times: Core SWE intern 2025, SDE-2 2022, and the LeetCode company tag.
- `data/questions/dsa/max-area-of-island.yaml`, followed by `max-area-after-filling-row-or-column.yaml` (hard).
- `evaluate-division`, `alien-dictionary`, `dijkstra-shortest-path`, `bellman-ford-shortest-path`.

**Design-a-data-structure / streaming**
- `lfu-cache`, `insert-delete-getrandom-o1` (with the follow-up "make it thread-safe; what does it cost"), `sliding-window-maximum` (price ticks), `sliding-window-median`, `streaming-top-k-sum`, `reservoir-sampling-stream`.

**Concurrency (closest to Akhil's strengths — make these flawless)**
- `lld/design-bounded-blocking-queue`, `java/lock-free-stack-atomic-reference`, `lld/task-scheduler-with-wait-and-workers`, `java/java-final-and-synchronized-methods`.

**Networking**
- `domain/tcp-vs-udp` (2 sightings), `domain/tcp-congestion-control`, `domain/reliable-multicast-protocol`, `domain/kernel-bypass-networking`.

**Memory / CPU / OS**
- `domain/virtual-address-aliasing-cache`, `domain/cpu-memory-load-path`, `domain/rdtsc-serialized-vs-non-serialized`, `domain/cpu-cache-hierarchy`, `domain/virtual-memory-page-faults`, `domain/tlb-hits-and-misses`, `domain/copy-on-write-page-counts`, `domain/ipc-mechanisms`, `domain/bankers-algorithm-deadlock`, `domain/branch-prediction-builtin-expect`, `domain/debug-binary-strace-ld-library-path`.

**C++ (asked even of generalists)**
- `cpp-virtual-functions-vtable`, `virtual-destructor`, `cpp-smart-pointers`, `cpp-rule-of-five-move-semantics` (2 sightings), `vector-contiguous-memory`, `stl-container-internals`, `pass-by-value-vs-reference`, `cpp-memory-leak-debugging`.

**Java / Spring (one 2025 report, but it is the only Java-track loop)**
- `java-equals-vs-double-equals-hashcode`, `java-hashmap-internals`, `java-string-immutability`, `java-stack-vs-heap-memory`, `deep-vs-shallow-copy`, `java-inheritance-oa`, `spring/jdbc-connection-pooling`.

**LLD / HLD**
- `lld/design-order-book` (2 sightings, both from guides), `lld/design-hft-system-observer-callbacks`, `lld/design-in-memory-file-system`, `lld/design-rate-limiter`.
- `hld/low-latency-word-count-over-slow-api` (the "1000 ms → 400 µs" question), `hld/rdbms-scaling-vs-nosql`, `hld/idempotency-in-payment-systems`, `hld/design-linkedin-jobs`, `hld/design-current-project-without-cloud`.

**OA hard problems (2026 new-grad; useful as a ceiling, not a priority for a senior)**
- `forward-token-routing-game` (game theory), `tree-path-mex-prime-sums`, `max-gcd-sum-after-root-path-removal`.

**Puzzles (old, 2016 intern; Tower now says it avoids "random logic problems")**
- `expected-max-updates-random-permutation`, `race-to-50-game`, `equidistant-points-on-sphere`.

## 5. Behavioural

Tower's stated values are Excellence, Respect, Innovation, Integrity and Teamwork
([about-us](https://tower-research.com/about-us/)). The candidate qualities it names are
entrepreneurial spirit, passion, clear communication, and calm and focus when the unexpected
occurs ([careers](https://tower-research.com/careers/)). For engineers it adds Take ownership,
Innovate constantly and Drive success, and prizes drive, communication, collaboration and passion
([engineering](https://tower-research.com/engineering/)). The only behavioural questions reported
are "Why Tower?", "Why change?" and "Walk me through your work". Most of the probing is technical
depth on your own projects.

| Value / quality | Story to map (Akhil to fill in) |
|-----------------|----------------------------------|
| Calm and focus when the unexpected occurs | A production incident: what you measured first and how you contained it. Keep the latency and throughput numbers ready. |
| Take ownership / drive success | A component you owned end to end, with a measurable p99 or throughput gain. |
| Innovation (proof-of-concept before adoption) | A new technology you evaluated with a PoC before rollout. Tower describes exactly this practice ([AMA](https://tower-research.com/ask-tower-anything-questions-and-answers-for-quants-engineers-and-other-prospective-employees/)). |
| Clear communication / collaboration | Working with a non-engineering stakeholder (traders here; ops/product for you) under time pressure. |
| Integrity / respect | A disagreement on a design that you resolved with data. |
| "Why Tower?" | Hub-and-spoke platform, engineers as owners, and the move from a framework-level to a systems-level engineer. Avoid "money" and "HFT is cool". |
| "Re-build your project without public cloud" | Prepare it. It was asked at the techno-managerial round ([1689730](https://leetcode.com/discuss/post/1689730/)). Tower runs on-prem and in colocation (bare-metal Kubernetes, "dozens of colocation centers"). |

## 6. Company-specific angle: latency and systems depth

**What they probe (from reports):**
1. **Networking:** TCP vs UDP; why market data uses UDP multicast; designing reliable multicast
   to N hosts (sequence numbers, NACK/retransmit, gap recovery); TCP congestion control; kernel
   bypass.
2. **Memory hierarchy:** L1/L2/L3 (why L1 is small and fast); the full path of a `mov` from memory
   (TLB → cache → memory controller); two virtual pages aliasing one physical page and cache
   coherence; page faults; TLB simulation; copy-on-write.
3. **Timing:** serialized vs non-serialized RDTSC. In other words, how you measure nanoseconds
   honestly.
4. **Language internals:** vtables and virtual-call cost, move semantics and vector reallocation,
   smart pointers. For Java: heap vs stack, String pool, HashMap internals.
5. **Lock-free concurrency:** a CAS-based stack, a bounded blocking queue, worker pools.
   Third-party guides add false sharing and lock-free structures as "HFT favourites"
   ([comment on 6834791](https://leetcode.com/discuss/post/6834791/)).
6. **Low-latency design reasoning:** "an API takes 1 s; answer in 400 µs" means precompute,
   index and cache near the consumer ([4177746](https://leetcode.com/discuss/post/4177746/)).
   An order book also needs justified data-structure choices for bids and asks.

**The gap a Java/Spring engineer must close:**
- **Get below the framework.** Spring Boot vocabulary earns nothing here. Rebuild your answers
  around what the JVM and OS actually do: object layout and headers, allocation and TLABs, GC
  pauses and why hot paths avoid allocation, `volatile`/happens-before, CAS and the ABA problem,
  false sharing and `@Contended`, cache-line padding (LMAX Disruptor is the canonical Java
  answer), and off-heap and memory-mapped files.
- **Learn enough C++ to talk about it.** Even non-C++ loops asked about vtables, vector
  contiguity, pass-by-value vs reference and smart pointers. Map each to its Java equivalent
  (virtual dispatch and inlining, ArrayList growth, references vs values, try-with-resources vs
  RAII). That way you can answer from first principles and admit the C++ specifics honestly.
- **Networking at the packet level:** sockets, TCP state and Nagle, UDP multicast, NIC → kernel →
  user-space copies, and what kernel bypass removes.
- **Measurement discipline:** p50/p99/p99.9, coordinated omission, and timestamp sources (RDTSC
  vs `System.nanoTime`).

## 7. What the job postings ask for

I fetched the 7 Gurugram engineering postings on Tower's Greenhouse board on 2026-10-07 (details
under `official.job_postings` in `company.yaml`). Skill counts across the 7:

| Skill | Count | Skill | Count |
|-------|-------|-------|-------|
| python | 7 | rust | 2 |
| linux | 7 | go | 2 |
| cpp | 5 | kafka | 2 |
| low-latency | 3 | distributed-systems | 2 |
| bash | 3 | iac / observability / ci-cd | 2 each |
| data-pipelines | 3 | networking / security / sql | 2 each |
| c, market-data, simulation, high-throughput | 2 each | **java** | **0** |

Linux and Python appear in every posting. C++ appears in 5 of the 7 (required in the two
trading-side roles). Java appears in none of the Gurugram postings. Across the whole global
board it appears only as "a plus" (Montreal C++ developer) or as one option in a list (Montreal
intern). Tower's own language statement is C++, Python and Rust
([AMA](https://tower-research.com/ask-tower-anything-questions-and-answers-for-quants-engineers-and-other-prospective-employees/)).

**Realistic fit for a 7-year Java/Spring engineer, and the gap for each role:**

| Posting | Fit | Gap to close |
|---------|-----|--------------|
| [Software Engineer III, Shared Services](https://www.tower-research.com/open-positions/?gh_jid=8029768) | **Best fit.** Microservices, Kafka, databases, web backend, on-call, 24/7 production. | **Rust is required.** You need real Rust (ownership, async/tokio, a service you have built), plus Go or Python, bare-metal deployment and DevOps. Office 4 days a week. |
| [Platform as a Service Engineer, Infrastructure](https://www.tower-research.com/open-positions/?gh_jid=8036383) | Medium. Distributed systems, Kafka and observability transfer. | 3–7 years of Kubernetes *admin* plus writing operators, Python/Go/Rust/C++ systems programming, etcd, DNS and storage. MS/PhD listed. |
| [Python Developer, QR&T](https://www.tower-research.com/open-positions/?gh_jid=6629676) | Medium–low. ETL, CI/CD and observability transfer. | Python data stack (NumPy, Pandas/Polars, Cython), plus the C++ compile/link/load process and Linux fundamentals. |
| [Quantitative Developer](https://www.tower-research.com/open-positions/?gh_jid=7102180) / [Low Latency Developer](https://www.tower-research.com/open-positions/?gh_jid=4357723) | Low for now. | Strong C/C++ is required. The low-latency role also wants 3–5 years in finance. This would be a 6–12 month C++ retooling. |
| SE II Core AI/ML, InfoSec III | Not a fit | Different discipline. |
| Historical Java roles (post-trade) | **Was a fit.** Tower hired Java engineers into its Post-Trade division ([2021, L5](https://leetcode.com/discuss/post/1476358/); [2025, SDE-1](https://leetcode.com/discuss/post/6375258/)). | No such opening is live right now. Ask recruiters or referrers directly about post-trade or back-office Java teams. |

## 6c. Tech stack and frameworks to prepare

Core and common items only (full list with evidence in `company.yaml` → `tech_stack`). Sources: the
7 Gurugram postings (re-checked on Greenhouse on 2026-10-08), Tower's
[AMA](https://tower-research.com/ask-tower-anything-questions-and-answers-for-quants-engineers-and-other-prospective-employees/)
and the interview reports. Tower's GitHub org has no public repos. Tower interviews go below the
framework, so most of the prep is OS, networking and latency rather than libraries.

| Group | Core | Common |
|---|---|---|
| **Frameworks & languages** | Python · Linux (systems, OS internals) · C/C++ | Rust · Bash/shell · production monitoring |
| **Patterns** | Low-latency, high-throughput systems | Market data (UDP multicast, gap recovery) · lock-free concurrency and mechanical sympathy · data pipelines / ETL |
| **Tools** | none | HackerRank (online test) |

**Revise** (Akhil already has these; take them down to first principles):

1. **Java concurrency down to CAS and the memory model** ([A6](../../academy/lessons/A6.md), [B2](../../academy/lessons/B2.md), [B3](../../academy/lessons/B3.md), [B7](../../academy/lessons/B7.md)): a Treiber stack with `AtomicReference`; explain ABA and happens-before without notes.
2. **JVM memory, GC and allocation-free hot paths** ([A2](../../academy/lessons/A2.md), [A4](../../academy/lessons/A4.md), [A5](../../academy/lessons/A5.md), [A8](../../academy/lessons/A8.md)): TLABs, young-gen pauses, an allocation-free hot path measured with JMH.
3. **Data structures and LLD for trading** ([E4](../../academy/lessons/E4.md), [E5](../../academy/lessons/E5.md)): an order book with justified bid/ask structures and a fill-callback interface.
4. **RDBMS depth** ([Q5](../../academy/lessons/Q5.md), [Q6](../../academy/lessons/Q6.md)): B-tree lookups and MVCC visibility, two minutes each.

**Learn** (new for Akhil, core or common here):

1. **Linux and OS internals** (no Academy module): page faults, TLB misses, scheduling ([kernel MM concepts](https://www.kernel.org/doc/html/latest/admin-guide/mm/concepts.html)).
2. **Networking for trading** (no Academy module): [udp(7)](https://man7.org/linux/man-pages/man7/udp.7.html), [ip(7)](https://man7.org/linux/man-pages/man7/ip.7.html) multicast, [tcp(7)](https://man7.org/linux/man-pages/man7/tcp.7.html) Nagle; design reliable multicast with sequence numbers and NACKs.
3. **Mechanical sympathy and latency measurement** ([A8](../../academy/lessons/A8.md), [B3](../../academy/lessons/B3.md)): the [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/) and [JEP 142](https://openjdk.org/jeps/142) (`@Contended`); p99.9 over the mean.
4. **C++ object model, enough to discuss** (no Academy module): vtables, move semantics, vector reallocation, RAII and smart pointers, mapped to Java ([learncpp](https://www.learncpp.com/), [Core Guidelines](https://isocpp.github.io/CppCoreGuidelines/CppCoreGuidelines)).
5. **Python for scripting and data work** (no Academy module): rewrite one small Java utility in Python; NumPy basics ([tutorial](https://docs.python.org/3/tutorial/index.html)).
6. **Rust** (no Academy module; required for SE III Shared Services): ownership and borrowing in [the book](https://doc.rust-lang.org/book/), then a small [tokio](https://tokio.rs/tokio/tutorial) service.
7. **Kafka for shared-services work** ([S9](../../academy/lessons/S9.md)): partitions, consumer groups and ordering for a trade-event pipeline ([intro](https://kafka.apache.org/intro)).

## 8. Strategy

Daily floor of about 25 minutes. Ordering follows the COMPANY-PREP §4 priority formula. This
section is only the narrative.

**Last 4 weeks**
- Week 1: Grid BFS/DFS (Shortest Bridge, Max Area of Island and its row/column follow-up) and
  LFU/LRU. Write each in Java and time yourself. The Tower rounds reward speed.
- Week 2: Concurrency drills in Java: bounded blocking queue with `ReentrantLock`/`Condition`, a
  Treiber stack with `AtomicReference` (plus ABA), and a fixed worker pool with `wait()`. Then
  explain false sharing and the Disruptor.
- Week 3: Systems theory, one topic a day. TCP/UDP/multicast, congestion control, virtual memory
  and TLB, the CPU cache hierarchy, page faults and COW, Banker's algorithm, RDTSC. Write
  5-sentence answers. The OA essay questions reward precise, short prose.
- Week 4: Design. Order book (price levels as `TreeMap`/arrays + per-level FIFO + id→order map
  for O(1) cancel), the 400 µs book-API question, and an HFT LLD with observers. Also do Java
  rapid-fire (equals/hashCode, HashMap internals, String pool, stack vs heap, connection pooling).

**Last week:** redo the 2-sighting items (Shortest Bridge, TCP vs UDP, rule of five, order book).
Practise one 90-minute mock OA in the "fix the bug with minimal changes" format. Prepare the
behavioural table above, especially "Why Tower?" and "Build it without public cloud".

**Day before:** review notes only. Re-read the vtable / JVM dispatch mapping and the memory-path
explanation. Set up HackerRank and a clean C++/Java environment. Some 2026 OAs required double
camera proctoring ([8395109](https://leetcode.com/discuss/post/8395109/)).

## 9. Sources

All accessed 2026-10-07. The report dates are the post dates.

- Official: [Careers](https://tower-research.com/careers/) · [About us / values](https://tower-research.com/about-us/) · [Culture](https://tower-research.com/culture/) · [Engineering](https://tower-research.com/engineering/) · [Internships](https://tower-research.com/internships/) · [Ask Tower Anything (May 2026)](https://tower-research.com/ask-tower-anything-questions-and-answers-for-quants-engineers-and-other-prospective-employees/) · [Greenhouse board API](https://boards-api.greenhouse.io/v1/boards/towerresearchcapital/jobs)
- LeetCode Discuss reports: [6375258 (2025-02)](https://leetcode.com/discuss/post/6375258/) · [6939917 (2025-07)](https://leetcode.com/discuss/post/6939917/) · [1702584 (2022-01)](https://leetcode.com/discuss/post/1702584/) · [1689730 (2022-01)](https://leetcode.com/discuss/post/1689730/) · [1764282 (2022-02)](https://leetcode.com/discuss/post/1764282/) · [2717817 (2022-10)](https://leetcode.com/discuss/post/2717817/) · [1867832 (2022-03)](https://leetcode.com/discuss/post/1867832/) · [1890926 (2022-03)](https://leetcode.com/discuss/post/1890926/) · [1537141 (2021-10)](https://leetcode.com/discuss/post/1537141/) · [959223 (2020-12)](https://leetcode.com/discuss/post/959223/) · [988571 (2020-12)](https://leetcode.com/discuss/post/988571/) · [5620964 (2024-08)](https://leetcode.com/discuss/post/5620964/) · [3933506 (2023-08)](https://leetcode.com/discuss/post/3933506/) · [4177746 (2023-10)](https://leetcode.com/discuss/post/4177746/) · [8214911 (2026-05)](https://leetcode.com/discuss/post/8214911/) · [8395109 (2026-07)](https://leetcode.com/discuss/post/8395109/) · [8472422 (2026-08)](https://leetcode.com/discuss/post/8472422/)
- Forum context: [6834791 (2025-06)](https://leetcode.com/discuss/post/6834791/) · [1476358 (2021-09)](https://leetcode.com/discuss/post/1476358/) · [1341458](https://leetcode.com/discuss/post/1341458/) · [1583793](https://leetcode.com/discuss/post/1583793/) · [7447700 (2025-12)](https://leetcode.com/discuss/post/7447700/)
- GeeksforGeeks: [telephonic intern round (2016)](https://www.geeksforgeeks.org/?p=131014) · [strategist (puzzles, no specifics)](https://www.geeksforgeeks.org/?p=117608)
- Third-party guides (no primary sources cited): [techinterview.org (2026-07)](https://www.techinterview.org/post/3233476792/how-tower-research-capital-interviews-engineers/) · [techprep (2026)](https://www.techprep.app/blog/tower-research-interview-process) · [quantt (2026-05)](https://www.quantt.co.uk/resources/tower-research-interview) · [tradermath (2026-07)](https://www.tradermath.org/articles/tower-research-capital-interview-guide) · [quantvault](https://quantvault.org/tower-research-interview-questions.html)
- Aggregate LeetCode lists: [liquidslr](https://github.com/liquidslr/leetcode-company-wise-problems) · [snehasishroy](https://github.com/snehasishroy/leetcode-companywise-interview-questions/tree/master/tower-research) (both list the same 3 problems)
- Blocked / not read: Code360 (403), Glassdoor (403), 1point3acres (403), Reddit (blocked), InterviewQuery (429)
