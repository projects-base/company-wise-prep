**Short answer:** Tell one real production failure: what broke, how you detected it, how you mitigated it, and the root cause. Then describe the fault-tolerance strategy you put in place (timeouts, retries with backoff, circuit breakers, idempotency, fallbacks, redundancy, alerting). On "does it cover every aspect": no strategy covers everything, so say which failure modes it handles, which it does not, and why that trade-off was acceptable.

## What they are checking

- **Real incident experience:** detect, mitigate, fix, prevent.
- **Fault-tolerance knowledge:** the right pattern for the right failure.
- **Honest limits:** you understand the gaps (correlated failures, a whole region down, data corruption, a poison message, the database itself).
- **Learning:** what changed after the incident.

## Structure

STAR + a short design discussion.

1. **Failure:** symptom, impact (users, duration, data), how it was detected.
2. **Mitigation:** what restored service first.
3. **Root cause.**
4. **Strategy after:** map each pattern to the failure it handles.
   - Slow dependency: timeouts, bulkheads (separate thread or connection pools), circuit breaker (for example Resilience4j).
   - Transient errors: retry with exponential backoff and jitter, only for idempotent operations or with idempotency keys.
   - Lost or duplicate messages: at-least-once delivery plus idempotent consumers, a dead-letter queue, the outbox pattern.
   - Instance or zone failure: multiple instances, health checks, load balancer failover.
   - Detection: alerts on error rate and latency, not only CPU.
5. **Gaps:** what is not covered and the accepted risk (RTO/RPO).

[F1 · Building blocks](../academy/lessons/F1.md) and [F2 · Distributed theory](../academy/lessons/F2.md) cover the patterns in more depth.

## Template

> "At [company], [service] failed when [trigger, e.g. a downstream API became slow]. [Impact, e.g. our thread pool filled up and all endpoints timed out for N minutes]. We noticed through [alert / customer report].
>
> I [mitigation, e.g. disabled the feature calling that dependency with a flag], which restored service in [time]. The root cause was [cause, e.g. no timeout on the HTTP client, so threads waited forever].
>
> Afterwards I [changes, e.g. added connect and read timeouts, a circuit breaker with a cached fallback, a separate pool for that client, and a latency alert].
>
> Does it cover everything? No. It handles [covered failures]. It doesn't handle [gaps, e.g. our own database going down, or a bad deploy that corrupts data]; for those we rely on [backups / rollback / runbooks]. We accepted that because [cost / likelihood reasoning]."

## Mistakes to avoid

- Claiming your design covers every failure.
- Retrying non-idempotent operations (double payments).
- Listing patterns with no link to the actual incident.
- Blaming another team for the outage.
- No numbers for impact or recovery time.
