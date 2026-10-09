**Short answer:** Put a `RateLimiter` interface in front of the algorithm, and have a `RateLimiterService` that looks up the right rule for each request and keeps one limiter per (rule, client) key. Code one algorithm well; token bucket is the usual pick because it allows short bursts and needs only two numbers per key. Per-API limits are just different rules. A limit shared by two APIs is one rule that both APIs map to, so they draw from the same bucket.

## Requirements

- `allow(clientId, api)` returns true or false, fast, in memory, on one node.
- Limits are configured per API: capacity and window (for example 100 per minute for `/search`, 10 per second for `/login`).
- Some APIs share one budget (for example `/upload` and `/import` together get 50 per minute).
- Algorithms are swappable: token bucket, fixed window, sliding window log, sliding window counter.
- Thread-safe under many concurrent requests.
- Out of scope at first: a distributed limit across nodes (see Extensions).

## Classes

- `RateLimitRule` (record): rule id, capacity, window. Pure config.
- `RuleRegistry`: maps an API path to a rule id. Two APIs mapped to the same rule id share a budget.
- `RateLimiter` (interface): `boolean tryAcquire()`. One instance holds the state for one key.
- `TokenBucketLimiter`, `SlidingWindowLogLimiter`, ...: implementations.
- `RateLimiterFactory`: builds a limiter for a rule (chooses the algorithm).
- `RateLimiterService`: the entry point. Key = `ruleId + ":" + clientId`. Keeps a `ConcurrentHashMap<String, RateLimiter>`.
- `Clock` (injected `LongSupplier` of nanos): makes the limiter testable without sleeping.

## Patterns used

- **Strategy**: `RateLimiter` implementations are interchangeable algorithms. Adding one does not touch the service (open/closed).
- **Factory**: `RateLimiterFactory` hides which class is built for a rule.
- **Registry / configuration object**: rules live in data, not in code, so new limits need no deploy if they are loaded from config.
- In Spring this sits in a `HandlerInterceptor` or a servlet filter, which returns HTTP 429 with a `Retry-After` header on rejection.

## Code

```java
public record RateLimitRule(String id, long capacity, Duration window) {}

public interface RateLimiter {
    boolean tryAcquire();
}

/** Token bucket with lazy refill: no background thread. */
public final class TokenBucketLimiter implements RateLimiter {
    private final long capacity;
    private final double tokensPerNano;
    private final LongSupplier nanoClock;
    private double tokens;
    private long lastRefill;

    public TokenBucketLimiter(RateLimitRule rule, LongSupplier nanoClock) {
        this.capacity = rule.capacity();
        this.tokensPerNano = (double) rule.capacity() / rule.window().toNanos();
        this.nanoClock = nanoClock;
        this.tokens = capacity;              // start full
        this.lastRefill = nanoClock.getAsLong();
    }

    @Override
    public synchronized boolean tryAcquire() {
        long now = nanoClock.getAsLong();
        tokens = Math.min(capacity, tokens + (now - lastRefill) * tokensPerNano);
        lastRefill = now;
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }
}

public final class RuleRegistry {
    private final Map<String, RateLimitRule> rulesById;
    private final Map<String, String> ruleIdByApi;   // "/upload" -> "bulk", "/import" -> "bulk"

    public RuleRegistry(Map<String, RateLimitRule> rulesById, Map<String, String> ruleIdByApi) {
        this.rulesById = Map.copyOf(rulesById);
        this.ruleIdByApi = Map.copyOf(ruleIdByApi);
    }

    public Optional<RateLimitRule> ruleFor(String api) {
        return Optional.ofNullable(ruleIdByApi.get(api)).map(rulesById::get);
    }
}

public final class RateLimiterService {
    private final RuleRegistry registry;
    private final Function<RateLimitRule, RateLimiter> factory;
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiterService(RuleRegistry registry, Function<RateLimitRule, RateLimiter> factory) {
        this.registry = registry;
        this.factory = factory;
    }

    public boolean allow(String clientId, String api) {
        return registry.ruleFor(api)
            .map(rule -> limiters
                .computeIfAbsent(rule.id() + ":" + clientId, k -> factory.apply(rule))
                .tryAcquire())
            .orElse(true);                    // no rule: not limited
    }
}
```

`computeIfAbsent` on `ConcurrentHashMap` creates exactly one limiter per key even under a race. The lock inside `tryAcquire` is per key, so different clients never contend. The check-and-take is one critical section, so two threads cannot both spend the last token.

## Extensions

- **Different APIs, different limits and windows (follow-up 1):** each API maps to its own `RateLimitRule`. The service code does not change; only the registry data does. A rule could also carry the algorithm name so one API uses a sliding window and another a token bucket.
- **One limit shared by two APIs (follow-up 2):** map both APIs to the same rule id. The limiter key is `ruleId:clientId`, not `api:clientId`, so both APIs hit the same bucket. If an API needs its own limit *and* the shared one, give it a list of rule ids and require all to pass. Take tokens only after every check passes, or refund on failure, so a rejected call does not burn the shared budget.
- **Algorithm trade-offs:** fixed window is cheapest but lets 2x the limit through around the boundary. Sliding window log is exact but stores a timestamp per request. Sliding window counter (current count plus the previous window's count weighted by overlap) is a close approximation with O(1) memory. Token bucket allows bursts up to capacity and then a steady rate.
- **Lock-free variant:** pack tokens and timestamp into one immutable state object in an `AtomicReference` and update with a CAS loop. Worth it only if the per-key lock shows up in profiles.
- **Memory:** idle keys pile up. Evict limiters not used for a while (a Caffeine cache with `expireAfterAccess`, or a periodic sweep). An evicted bucket simply starts full again, which is safe.
- **Distributed:** with many nodes, keep the state in Redis and run the refill-and-take as one Lua script so it is atomic. Trade-off: a network hop per request. A local pre-check plus periodic sync cuts that cost at the price of some overshoot.

Deeper reading: [B7 · Classic problems: producer-consumer, bounded queue, concurrent LRU, rate limiter](../academy/lessons/B7.md), [E6 · LLD case studies](../academy/lessons/E6.md), [F4 · Case studies: URL shortener, rate limiter, notification system](../academy/lessons/F4.md).
