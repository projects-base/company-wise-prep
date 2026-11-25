**Short answer:** The section is 15 questions in about 20 minutes, so 80 seconds each: answer what you know, mark the rest, come back once. The topics reported are REST, testing, microservices, system design, distributed systems and algorithms. Learn the small facts below cold, because most wrong answers come from two options that both look right.

**Note:** the 15 questions at the end are **practice written for this bank**. They are **not** reported Deliveroo questions; no real question text was found.

## Explanation

**How to spend 20 minutes**
- **First pass (12 min):** answer in 45 seconds or mark and move on.
- **Second pass (6 min):** marked ones. Cross out two options, then choose.
- **Last 2 min:** nothing left blank, unless the page says wrong answers cost marks.
- "Select all that apply" is often all-or-nothing. Read the question type before the options.
- Watch for *not*, *always*, *best* and *first*.

**REST and HTTP**
- Safe (no change asked): `GET`, `HEAD`, `OPTIONS`. Idempotent: those plus `PUT` and `DELETE`. `POST` and `PATCH` are not guaranteed to be.
- Idempotent means the same **effect**, not the same response. A second `DELETE` may return 404.
- 201 created (with `Location`), 202 accepted but not done, 204 no body, 304 not modified.
- 400 malformed, 401 not authenticated, 403 not allowed, 404 not found, 405 wrong method, 409 conflict, 412 precondition failed, 415 wrong media type, 422 understood but invalid, 429 too many.
- 502 bad answer from upstream, 503 unavailable now, 504 upstream timed out.
- 301 and 302 may turn a `POST` into a `GET`; 307 and 308 keep the method.
- `ETag` with `If-None-Match` saves bandwidth; with `If-Match` it prevents lost updates.
- `Cache-Control: no-cache` means store but check first; `no-store` means do not store.
- CORS is enforced by the **browser**. It does not protect the server.
- Offset pagination slows down and shifts when rows are added; a cursor does not.

**Testing**
- Pyramid: many unit, fewer integration, few end-to-end.
- Dummy fills a parameter, stub returns canned answers, mock checks calls, spy wraps the real object, fake is a small working version.
- Contract test: do consumer and provider agree, without running both together?
- Coverage shows what ran, not what was checked. Mutation testing checks the checks.
- TDD loop: red, green, refactor.
- Regression test: old things still work. Smoke test: does it start?
- Load is expected traffic, stress is beyond it, soak is for hours.
- Boundary value analysis tests just below, at and just above a limit.

**Microservices**
- One database per service. Sharing tables couples deployments.
- Saga: local transactions plus **compensations**; no isolation between steps. Orchestration has a conductor, choreography has events.
- Outbox: write the event in the same transaction as the data, publish later. It gives at-least-once, so consumers must be idempotent.
- Circuit breaker: closed, open, half-open. Bulkhead: separate pools.
- Retry only transient faults, with backoff and jitter.
- API gateway: one entry for routing, auth, limits. Strangler fig: replace a monolith piece by piece.
- Liveness failing restarts the container; readiness failing only stops traffic.

**System design**
- 99.9% is about 43 minutes a month, 99.99% about 4.3. In series availabilities multiply: 0.99 x 0.99 = 0.9801.
- Cache-aside: the app fills it on a miss. Write-through: cache and store together. Write-back: store later, can lose data.
- Stampede: many misses on one key. Fix with a single loader, early refresh, jittered TTL.
- Layer 4 balances connections; layer 7 understands HTTP.
- Consistent hashing: adding a node moves about 1/N of the keys, not almost all.
- Token bucket allows bursts; leaky bucket smooths; fixed window lets through up to twice the limit around the edge.
- Read replicas scale reads and lag; sharding scales writes.

**Distributed systems**
- CAP: in a partition choose consistency or availability. PACELC: otherwise choose latency or consistency.
- Quorum: `R + W > N` makes read and write sets overlap.
- Raft and majority quorums: `2f + 1` nodes survive `f` crashes. Byzantine faults need `3f + 1`.
- Two-phase commit blocks if the coordinator dies after "prepared".
- At-most-once can lose, at-least-once can repeat. "Exactly once" is at-least-once plus idempotency or transactions inside one system.
- Kafka: order is kept per partition; a group has at most one active consumer per partition.
- Lamport clocks order events, but cannot tell you two events were concurrent; vector clocks can.
- Clocks differ between machines, so do not order events by wall time.

**Algorithms**
- Binary search O(log n) on sorted data; heap push and pop O(log n), peek O(1), build O(n).
- Merge sort: stable, O(n log n), O(n) space. Quicksort: worst case O(n^2), not stable. `Arrays.sort` on objects is stable.
- Hash map O(1) on average; in Java a bucket of 8 or more turns into a tree (table size at least 64).
- BFS: shortest path with equal weights. Dijkstra: no negative weights. Bellman-Ford: negative weights, finds negative cycles.
- Topological sort needs a directed graph with no cycle.

## Example

One question, worked the way you should in 80 seconds. *A payment API call timed out. What should the client do?*

- "Retry at once" and "Report failure" both assume you know the outcome. You do not.
- "Retry with the same idempotency key" is safe in both cases, so it is the answer.
- The general move: for each option ask "what if the call **did** succeed?"

## Practice questions

Written for this bank, **not reported**. Try all 15 in 20 minutes; click an option to see the reason.

```quiz
{"question": "1. Which HTTP method is idempotent but not safe?", "level": "easy",
 "options": [
   {"text": "GET", "why": "Idempotent and safe."},
   {"text": "POST", "why": "Neither: two POSTs may create two resources."},
   {"text": "PUT", "correct": true, "why": "It changes state, and repeating it leaves the same state."},
   {"text": "PATCH", "why": "Not guaranteed idempotent, for example \"add 1\"."}]}
```

```quiz
{"question": "2. A client sends valid credentials but its role may not delete orders. Which status fits best?", "level": "easy",
 "options": [
   {"text": "403 Forbidden", "correct": true, "why": "We know who you are and you may not do this."},
   {"text": "401 Unauthorized", "why": "That means not authenticated, in spite of its name."},
   {"text": "409 Conflict", "why": "That is a clash with the current state of the resource."},
   {"text": "400 Bad Request", "why": "The request itself is well formed."}]}
```

```quiz
{"question": "3. Two people edit the same menu. The second save must not silently overwrite the first. Which is the HTTP way?", "level": "medium",
 "options": [
   {"text": "Send If-None-Match with the ETag; the server answers 304", "why": "That is for reads: it avoids downloading the same body again."},
   {"text": "Use Cache-Control: no-store on the GET", "why": "Fresh reads do not stop a later overwrite."},
   {"text": "Use PUT instead of PATCH", "why": "PUT replaces everything, so the overwrite gets worse."},
   {"text": "Send If-Match with the ETag; the server answers 412 if it changed", "correct": true, "why": "A conditional write is optimistic locking over HTTP."}]}
```

```quiz
{"question": "4. Offset pagination (page 2, size 20, newest first). A new item arrives between the two calls. What does the client see?", "level": "medium",
 "options": [
   {"text": "One item is skipped", "why": "That happens when an item is deleted, not added."},
   {"text": "The last item of page 1 again, at the top of page 2", "correct": true, "why": "Everything moved down one place. A cursor avoids it."},
   {"text": "Nothing changes, pages are snapshots", "why": "Each call runs a new query."},
   {"text": "409 Conflict", "why": "The server does not notice."}]}
```

```quiz
{"question": "5. A test needs a payment gateway that always answers \"declined\". It does not check calls. Which double is it?", "level": "easy",
 "options": [
   {"text": "Mock", "why": "A mock also checks how it was called."},
   {"text": "Stub", "correct": true, "why": "Canned answers, no expectations about calls."},
   {"text": "Spy", "why": "A spy wraps a real object and records calls."},
   {"text": "Fake", "why": "A fake has working logic, such as an in-memory repository."}]}
```

```quiz
{"question": "6. A suite has 95% line coverage, yet bugs slip through and most tests have no meaningful assertions. What shows this best?", "level": "medium",
 "options": [
   {"text": "Branch coverage", "why": "It still measures what ran, not what was asserted."},
   {"text": "More end-to-end tests", "why": "Slower, and they do not show which unit tests are empty."},
   {"text": "Load testing", "why": "It measures speed under traffic."},
   {"text": "Mutation testing", "correct": true, "why": "It changes the code on purpose; tests that still pass check nothing."}]}
```

```quiz
{"question": "7. Order is saved, then the service crashes before it publishes OrderPlaced. Which pattern closes this gap?", "level": "medium",
 "options": [
   {"text": "Transactional outbox", "correct": true, "why": "The event is a row in the same transaction, published later."},
   {"text": "Publish first, then save", "why": "Now a crash leaves an event for an order that does not exist."},
   {"text": "Circuit breaker", "why": "It limits calls to a failing dependency; it cannot make two writes atomic."},
   {"text": "Retry the HTTP request three times", "why": "The process is gone, and a new one does not know what to retry."}]}
```

```quiz
{"question": "8. What does a saga NOT give you, compared with one ACID transaction?", "level": "hard",
 "options": [
   {"text": "Durability of each step", "why": "Each local transaction is durable."},
   {"text": "A way to undo work", "why": "Compensations do that, as new actions."},
   {"text": "Isolation: others can see the state between steps", "correct": true, "why": "Each local transaction commits, so in-between states are visible."},
   {"text": "Atomicity inside one service", "why": "Each local step is an ordinary transaction."}]}
```

```quiz
{"question": "9. A dependency is down and every thread waits 30 s for it. What stops the failure from taking your whole service down soonest?", "level": "medium",
 "options": [
   {"text": "More retries", "why": "They multiply the load and the waiting."},
   {"text": "A bigger thread pool", "why": "It fills up a little later."},
   {"text": "A longer timeout", "why": "Threads are held even longer."},
   {"text": "A short timeout plus a circuit breaker", "correct": true, "why": "Fail fast, free the threads, and stop calling for a while."}]}
```

```quiz
{"question": "10. A request passes through three services in series, each available 99.9%. About how available is the request?", "level": "easy",
 "options": [
   {"text": "99.9%", "why": "Only if one of them were involved."},
   {"text": "99.7%", "correct": true, "why": "0.999 cubed is about 0.997."},
   {"text": "99.99%", "why": "Chaining lowers availability; redundancy raises it."},
   {"text": "99.97%", "why": "A slip of ten: three times 0.1% is 0.3%."}]}
```

```quiz
{"question": "11. Ten cache nodes, keys placed by hash(key) mod 10. You add one node. About how many keys change node?", "level": "medium",
 "options": [
   {"text": "About 91%", "correct": true, "why": "A key stays only if both remainders agree: about 1 in 11. Consistent hashing moves about 9%."},
   {"text": "About 9%", "why": "That is consistent hashing."},
   {"text": "About 50%", "why": "That is roughly what doubling the nodes would do."},
   {"text": "None, old keys stay", "why": "The formula changed for every key."}]}
```

```quiz
{"question": "12. N = 3 replicas, W = 2. Which R makes every read overlap with the latest successful write?", "level": "medium",
 "options": [
   {"text": "1", "why": "1 + 2 = 3, which is not greater than 3."},
   {"text": "0", "why": "No replica would be read."},
   {"text": "2", "correct": true, "why": "R + W > N: 2 + 2 > 3. (Smallest R that works.)"},
   {"text": "It depends on the partition count", "why": "The overlap rule is per key and its N replicas."}]}
```

```quiz
{"question": "13. A Kafka topic has 6 partitions. One consumer group has 8 consumers. Which is true?", "level": "hard",
 "options": [
   {"text": "All 8 consume and share partitions", "why": "Classic consumer groups never share a partition."},
   {"text": "6 consume, 2 are idle, and order holds within each partition", "correct": true, "why": "A partition goes to one consumer of the group."},
   {"text": "Order holds across the whole topic", "why": "Only inside a partition."},
   {"text": "The group fails to start", "why": "Extra consumers wait as standby."}]}
```

```quiz
{"question": "14. In a 5-node Raft cluster a partition leaves the old leader with 1 other node. What happens?", "level": "hard",
 "options": [
   {"text": "Both sides accept writes and merge later", "why": "That would be split brain; the majority rule prevents it."},
   {"text": "The whole cluster stops", "why": "3 of 5 is still a majority."},
   {"text": "The old leader keeps committing until its term ends", "why": "It has no majority, so nothing it accepts can commit."},
   {"text": "The 3-node side elects a leader; the old leader cannot commit writes", "correct": true, "why": "Commit needs a majority, 3 of 5."}]}
```

```quiz
{"question": "15. You need the 100 largest of 10 million numbers from a stream. Which is best?", "level": "medium",
 "options": [
   {"text": "A min-heap of size 100, O(n log 100)", "correct": true, "why": "Keep the smallest of the best on top and replace it."},
   {"text": "Sort everything, O(n log n)", "why": "Correct, but more time and all the memory."},
   {"text": "A max-heap of all numbers", "why": "It needs memory for all 10 million."},
   {"text": "A hash map of counts", "why": "Counts say nothing about order."}]}
```

## Pitfalls and follow-ups

- **`GET` with a body, 200 with an error inside, verbs in URLs:** typical answers to "which is not RESTful?".
- **401 or 403?** Who are you, against you may not.
- **PUT or PATCH?** Replace the whole resource, against change a part.
- **Cohesion high or low?** High cohesion, low coupling.
- **Authentication or authorisation?** Identity, against permission.
- **Horizontal or vertical scaling?** More machines, against a bigger machine.
- **Latency or throughput?** Time of one request, against requests per second. Batching helps the second and may hurt the first.
- **Strong or eventual?** Every read sees the last write, against replicas agree later.
- **Wrong in practice?** Write down the rule, not the question, and redo it in two days.

Go deeper: [W2 · REST API design](../academy/lessons/W2.md), [D12 · Testing Spring Boot](../academy/lessons/D12.md), [S8 · Microservices](../academy/lessons/S8.md), [F2 · Distributed theory: CAP, consistency, consensus](../academy/lessons/F2.md).
