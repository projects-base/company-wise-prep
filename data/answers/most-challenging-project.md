**Short answer:** Pick one backend system you know end to end and can draw from memory. Explain the problem and its scale, your exact role, the main design decisions with the alternatives you rejected, the hardest technical and non-technical challenges, the measured outcome, and one thing you would do differently. Expect the interviewer to interrupt and go deep at any point.

## What they are checking

- **Ownership:** what *you* did, not what the team did.
- **Design depth:** trade-offs, not just a list of technologies.
- **Numbers:** traffic, data size, latency, error rates, team size, timeline.
- **Honesty and reflection:** "what would you do differently" tests whether you can criticise your own design.

## Structure

A project walkthrough, about 5 minutes before questions:

1. **Context (30s):** business problem, users, scale.
2. **Your role (15s):** say "I" for your parts, "we" for the team's.
3. **Architecture (90s):** draw it: clients, services, data stores, queues, external systems.
4. **Design considerations (90s):** 2-3 decisions with alternatives (sync vs async, SQL schema, caching, idempotency, consistency).
5. **Challenges (60s):** one technical (a performance or data issue) and one human (a dependency, unclear requirements).
6. **Outcome:** measured.
7. **Do differently:** one specific change and why.

Prepare a one-line answer for: how did you test it, how is it monitored, what happens if a dependency is down, how would it handle 10x traffic.

## Template

> "The most challenging was [project] at [company]. It [what it does] for [users], handling about [scale, e.g. N requests/sec, N records/day].
>
> I was [role] and owned [parts]. The team was [size].
>
> Architecture: [components, e.g. Spring Boot services behind a gateway, PostgreSQL, a Kafka topic for events, Redis cache].
>
> Key decisions:
> - [Decision 1] instead of [alternative], because [reason].
> - [Decision 2], with the trade-off [cost].
>
> The hardest part was [technical challenge]. I [what you did], and [result with number].
> Separately, [non-technical challenge] which I handled by [action].
>
> Outcome: [metrics].
>
> What I'd do differently: [e.g. design for idempotency from day one instead of retrofitting it / add load tests before launch, not after the first incident]."

## Mistakes to avoid

- A project you only partly understand; deep-dives will expose it.
- Listing technologies without reasons.
- "We" throughout so your role is invisible.
- No numbers at all.
- "I wouldn't change anything."
