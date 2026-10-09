**Short answer:** Redis is an in-memory key-value store. One main thread executes commands one at a time over an event loop, so each command is atomic and there are no locks. Values are rich data structures (strings, hashes, lists, sets, sorted sets, streams), each with a compact encoding for small sizes. When memory is full it evicts keys with an *approximate* LRU or LFU that samples a few keys rather than tracking a perfect order. Keys with a TTL expire lazily on access and through a periodic sampling job.

## Explanation

**Threading.** Command execution is single-threaded. It uses I/O multiplexing (epoll on Linux) to serve thousands of connections. Since Redis 6, network reads and writes can use I/O threads, but commands still run on one thread. Because it is in memory and avoids locks, a simple GET or SET usually takes microseconds. One slow command (`KEYS *`, a huge `SMEMBERS`) blocks everyone, so use `SCAN`.

**Data structures.**
- Strings: binary-safe, used for counters (`INCR` is atomic) and cached JSON.
- Hashes, lists, sets: small ones use a compact listpack encoding (ziplist before Redis 7); large ones switch to a hash table or quicklist.
- Sorted sets: a skip list plus a hash table. That gives O(log n) insert and rank queries, which is why they are used for leaderboards.
- The main keyspace is a hash table that rehashes incrementally, a little on each operation, so a resize never pauses the server.

**Eviction (`maxmemory-policy`).** `noeviction` (writes fail), `allkeys-lru`, `volatile-lru` (only keys with a TTL), `allkeys-lfu`, `volatile-lfu`, `allkeys-random`, `volatile-random`, `volatile-ttl`. LRU is approximate. Each key stores a small access clock. On eviction Redis samples `maxmemory-samples` keys (default 5) and evicts the best candidate from a pool. LFU stores a logarithmic counter that also decays over time, so old hot keys cool down.

**Expiry.** Passive: a key is checked when accessed and deleted if expired. Active: a background cycle samples keys with a TTL and deletes the expired ones, repeating while many are found.

**Persistence and HA.** RDB snapshots (fork plus copy-on-write) and/or an AOF append log. Replication is asynchronous, so a failover can lose recent writes. Sentinel handles failover. Redis Cluster shards keys across 16384 hash slots.

## Example

```java
// Cache-aside with a TTL and a little jitter to avoid synchronized expiry
public Market getMarket(long id) {
    String key = "market:" + id;
    String cached = redis.opsForValue().get(key);
    if (cached != null) return json.read(cached, Market.class);
    Market m = repo.findById(id).orElseThrow();
    Duration ttl = Duration.ofSeconds(60 + ThreadLocalRandom.current().nextInt(10));
    redis.opsForValue().set(key, json.write(m), ttl);
    return m;
}
```

## Pitfalls and follow-ups

- **Approximate LRU/LFU, why?** A true LRU needs a linked list updated on every read, which costs memory and time. Sampling is close enough in practice. LFU suits stable hot sets; LRU suits recency-driven access.
- **Cache-aside vs write-through?** Cache-aside: the app reads the cache, loads from the DB on a miss, and on write updates the DB then deletes the key. Write-through: every write goes to the cache and the DB together, so the cache stays warm but writes are slower and the cache holds data nobody reads. Delete rather than update on write, so racing writers cannot leave a stale value.
- **Cache stampede?** A hot key expires and hundreds of requests hit the DB at once. Fixes: a per-key lock or single-flight (`SET key lock NX PX 5000`), TTL jitter, or refreshing early in the background before expiry.
- **Is Redis a source of truth?** Not for a wallet balance. Replication is async and eviction may drop keys. Keep money in PostgreSQL. Use Redis for caches, rate limits and session data.
- **Atomic multi-step logic?** A Lua script or `MULTI/EXEC`. Both run without other commands interleaving.

Further reading: [F1 · Building blocks](../academy/lessons/F1.md), [F6 · Distributed cache case study](../academy/lessons/F6.md).
