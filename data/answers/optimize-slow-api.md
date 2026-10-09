**Short answer:** I measure before I change anything. I look at p95/p99 latency for the endpoint and pull a few slow traces to see where the time goes: database, downstream calls, serialisation, or waiting for a thread or connection. I fix the biggest span first (a missing index, an N+1, a slow dependency, an exhausted pool), then verify with the same metrics under the same load. A slow dependency must never take the whole API down, so I add timeouts, a circuit breaker and a bulkhead.

## Explanation

**1. Define "slow".** Averages hide the problem. Look at p50, p95 and p99 per endpoint, plus throughput and error rate. Check whether it is slow all the time, only under load, or only for some inputs (a big customer, a large page size).

**2. Find where the time goes.** A distributed trace (OpenTelemetry, or Micrometer Tracing in Spring Boot 3) splits one request into spans: controller, each SQL query, each HTTP call. The longest span is the bottleneck. Common causes:

- **Database:** a sequential scan where an index should be used, an N+1 query loop, lock waits, or too many rows fetched. Check with `EXPLAIN (ANALYZE, BUFFERS)` and `pg_stat_statements`.
- **Pool exhaustion:** HikariCP `hikaricp.connections.pending` above zero and a high `connections.acquire` time mean requests wait for a connection, not for the query. Tomcat thread saturation looks similar.
- **Downstream calls:** made one after another when they could run in parallel, or with no timeout.
- **Payload:** huge JSON responses, no pagination, no compression.
- **JVM:** long GC pauses or CPU saturation (check GC pause metrics, and use JFR for a profile).

**3. Fix in order of cost.** Query and index fixes first, then remove N+1 (fetch join, batch fetch), paginate, run independent calls in parallel, cache stable reads, and move non-critical work (emails, audit) to async or a queue.

**4. Verify.** Same dashboard, same load test. Compare p99 before and after. Add an alert on the SLO so a regression is caught.

## Example

```java
// Before: two calls in sequence, no timeout -> latency = a + b, can hang forever
var profile = profileClient.get(id);
var limits  = limitsClient.get(id);

// After: parallel, each with its own timeout
var p = CompletableFuture.supplyAsync(() -> profileClient.get(id), ioPool)
        .orTimeout(300, TimeUnit.MILLISECONDS);
var l = CompletableFuture.supplyAsync(() -> limitsClient.get(id), ioPool)
        .orTimeout(300, TimeUnit.MILLISECONDS);
return new Account(p.join(), l.join()); // latency ~ max(a, b)
```

## Pitfalls and follow-ups

- **Which metrics and traces first?** The RED metrics per endpoint (rate, errors, duration as p95/p99), then a slow trace's span breakdown. Then pool metrics (Hikari pending and acquire time, Tomcat busy threads), GC pause time, and `pg_stat_statements` sorted by total time.
- **Stopping a slow dependency taking the API down?** A **timeout** on every outbound call (connect and read). A **circuit breaker** (Resilience4j) that opens after a failure or slow-call rate threshold and fails fast with a fallback. A **bulkhead**: a separate, bounded thread pool or semaphore per dependency, so one slow service cannot use up all request threads. Retry only idempotent calls, with backoff and jitter, and inside a total time budget.
- **Caching: what, where, invalidation?** Cache data that is read often and changes rarely (reference data, config, user profile). Use an in-process cache (Caffeine) for tiny, hot data and Redis for data shared across instances. Invalidate with a TTL plus evict-on-write (cache-aside). Never cache money balances you then trust for a debit.
- **Pitfall:** optimising the wrong thing. Without a trace you guess, and guesses are usually wrong.
- **Pitfall:** a bigger connection pool often makes things worse. The database has a fixed number of cores, so more parallel queries mean more contention.

Further reading: [Q7 · Query performance in practice](../academy/lessons/Q7.md), [F1 · Building blocks](../academy/lessons/F1.md), [H3 · Debugging and production habits](../academy/lessons/H3.md), [D8 · Production readiness](../academy/lessons/D8.md).
