**Short answer:** Classify the misses. Compulsory misses (first access) no cache fixes. Capacity misses go away with more memory; policy misses go away with a smarter eviction policy. The practical test: replay a sample of the real access trace through a simulator at several cache sizes and with several policies (LRU, LFU, W-TinyLFU, and the theoretical optimum, Belady's MIN). If hit ratio climbs steeply with size, it is capacity. If MIN or a frequency-aware policy beats LRU by a lot at the *same* size, it is the policy. Also check the cheap causes first: TTLs too short, bad keys, and evictions vs expirations.

## Explanation

**Step 1 - look at what the cache already tells you.**
- Evictions vs expirations: in Redis, `INFO stats` shows `evicted_keys`, `expired_keys`, `keyspace_hits`, `keyspace_misses`. Lots of expirations and few evictions means the TTL, not size or policy, is the problem.
- Memory: is the cache at its limit (`used_memory` vs `maxmemory`)? If it never fills, more size will not help.
- Key design: per-request ids, timestamps or unnormalised parameters in keys produce unique keys that never hit.

**Step 2 - understand the workload.**
- **Compulsory misses**: count distinct keys in a window. If most requests are for keys seen once, the ceiling is low whatever you do.
- **Reuse distance**: for each access, how many distinct keys were touched since the last access to the same key. Under LRU, an access hits if its reuse distance is smaller than the cache size. A histogram of reuse distances *is* the LRU hit-ratio curve for every size at once (Mattson's stack algorithm).

**Step 3 - simulate.**
- Plot hit ratio vs size (miss-ratio curve) for LRU. A curve that keeps rising means capacity-bound. A plateau means more memory is wasted.
- At the current size, compare LRU against LFU / W-TinyLFU and Belady's MIN (evict the key used furthest in the future; needs the trace, gives the upper bound). A large gap between LRU and MIN with the same size means the policy is the problem.
- Typical policy failures: a **scan** (batch job, crawler) flushes the LRU cache of the hot set; a frequency-aware or scan-resistant policy fixes it. Many one-hit wonders: admission filters (TinyLFU) avoid caching them at all.

**Step 4 - experiment in production carefully:** shadow cache with a different policy, or A/B a fraction of traffic.

## Example

```java
// Minimal LRU simulator to build a miss-ratio curve from a trace
static double lruHitRatio(List<String> trace, int capacity) {
    var lru = new LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> e) {
            return size() > capacity;
        }
    };
    int hits = 0;
    for (String k : trace) {
        if (lru.get(k) != null) hits++; else lru.put(k, true);
    }
    return (double) hits / trace.size();
}
// for (int c : List.of(1_000, 10_000, 100_000)) print(c, lruHitRatio(trace, c));
```

## Pitfalls and follow-ups

- **Hit ratio is not the only goal.** Weight misses by their cost (a miss that hits a slow service matters more); byte hit ratio matters when objects differ in size.
- **Cold start after deploy/restart** distorts metrics; measure in steady state.
- **Sharded / distributed cache:** a hot key or uneven hashing means one node evicts heavily while others are empty.
- **Redis policies?** `maxmemory-policy` options include `allkeys-lru`, `allkeys-lfu`, `volatile-lru`, `volatile-ttl` and `noeviction`; Redis LRU/LFU are approximations based on sampling.
- **In-process Java cache?** Caffeine uses W-TinyLFU and exposes `stats()` with hit and eviction counts.

Go deeper: [F6 · Distributed cache case study](../academy/lessons/F6.md), [F1 · Building blocks](../academy/lessons/F1.md).
