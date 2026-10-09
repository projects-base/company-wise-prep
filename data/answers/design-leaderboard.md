**Short answer:** Keep two structures: a `HashMap<playerId, score>` for O(1) score lookups, and a sorted set of `(score desc, playerId)` for ordering. An update removes the old pair from the sorted set and inserts the new one, both O(log n). Top-K walks the first K elements of the sorted set. A priority queue alone is weak here because it cannot update or remove an arbitrary player efficiently, and it cannot answer "what is my rank". At scale, the same idea is a Redis sorted set (`ZADD`, `ZREVRANGE`, `ZREVRANK`).

## Requirements

- `addScore(playerId, delta)` or `setScore(playerId, score)`.
- `top(k)`: the K best players with scores.
- `rank(playerId)`: the player's position (1-based).
- `reset(playerId)` / remove.
- Ties: higher score first; on equal score, earlier-achieved or lower id first (state the rule).
- Many updates, frequent reads of top 10. Thread-safe.

## Classes

- `PlayerScore` (record): playerId, score, updatedAt. Comparable by score desc, then updatedAt asc, then id.
- `Leaderboard` (interface): `update`, `top`, `rank`, `remove`.
- `InMemoryLeaderboard`: `HashMap` plus `TreeSet<PlayerScore>`, one read-write lock.
- `RedisLeaderboard`: same interface over a sorted set (for the distributed version).

## Patterns used

- Interface + two implementations (**Strategy** / Dependency Inversion): the game service does not care whether ranks come from memory or Redis.
- Composite key ordering in a balanced BST (red-black tree in `TreeSet`), which is the honest "data structure choice" this question tests.

**Why not just a heap**

| Operation | Max-heap (`PriorityQueue`) | `TreeSet` + `HashMap` |
|---|---|---|
| Update one player | O(n) remove + O(log n) insert | O(log n) remove + O(log n) insert |
| Top K | O(K log n) by polling and re-adding, or copy | O(K) iteration |
| Rank of a player | O(n log n) | O(n) with `TreeSet`; O(log n) with an order-statistic tree |
| Top K over a stream, no updates | best fit: min-heap of size K | overkill |

So: a heap is right for "top K from a one-off stream", the sorted set is right for a live leaderboard.

## Code

```java
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

record PlayerScore(String playerId, long score, long updatedAt) {}

final class InMemoryLeaderboard {
    private static final Comparator<PlayerScore> ORDER =
            Comparator.comparingLong(PlayerScore::score).reversed()
                      .thenComparingLong(PlayerScore::updatedAt)
                      .thenComparing(PlayerScore::playerId);

    private final Map<String, PlayerScore> byPlayer = new HashMap<>();
    private final TreeSet<PlayerScore> ranking = new TreeSet<>(ORDER);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long tick;                                     // logical time for tie-break

    public void addScore(String playerId, long delta) {
        lock.writeLock().lock();
        try {
            PlayerScore old = byPlayer.get(playerId);
            long newScore = (old == null ? 0 : old.score()) + delta;
            if (old != null) ranking.remove(old);         // must remove with the OLD key
            PlayerScore now = new PlayerScore(playerId, newScore, ++tick);
            byPlayer.put(playerId, now);
            ranking.add(now);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<PlayerScore> top(int k) {
        lock.readLock().lock();
        try {
            List<PlayerScore> out = new ArrayList<>(k);
            for (PlayerScore ps : ranking) {
                if (out.size() == k) break;
                out.add(ps);
            }
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** 1-based rank, or -1. O(n) with TreeSet.headSet(...).size(). */
    public int rank(String playerId) {
        lock.readLock().lock();
        try {
            PlayerScore ps = byPlayer.get(playerId);
            return ps == null ? -1 : ranking.headSet(ps).size() + 1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void remove(String playerId) {
        lock.writeLock().lock();
        try {
            PlayerScore old = byPlayer.remove(playerId);
            if (old != null) ranking.remove(old);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

The bug interviewers look for: mutating a score field inside an element that is already in a `TreeSet` breaks the tree's ordering. Records are immutable, so you are forced to remove the old element and insert a new one.

`headSet(ps).size()` is O(n) in the JDK because `TreeMap` does not store subtree sizes. For O(log n) rank you need an order-statistic tree (subtree counts) or a Fenwick tree over score buckets when scores are bounded integers.

## Extensions

- **Distributed / HLD version (the Noida HLD round):** Redis sorted set per leaderboard. `ZINCRBY board delta player`, `ZREVRANGE board 0 9 WITHSCORES` for top 10, `ZREVRANK board player` for rank, all O(log n) (range is O(log n + K)). Redis skip lists keep rank queries cheap. Persist the source-of-truth scores in Postgres; Redis can be rebuilt.
- **Very large boards (100M players):** exact rank for everyone is costly. Keep exact top N in a sorted set and estimate rank for others from a score histogram (count of players per score bucket).
- **Time-windowed boards (daily, weekly):** one sorted set per window key (`board:2026-10-09`) with an expiry; weekly can be computed with `ZUNIONSTORE` over daily sets.
- **Hot reads of top 10:** cache the top-10 list and refresh every second; most clients do not need real-time exactness.
- **Write contention:** the single write lock serialises updates. Options: batch updates on one writer thread fed by a queue, or shard by player and merge top-K from shards (each shard's top K, then a K-way merge).
- **Ties by "who got there first":** the `updatedAt` tie-break handles it; with Redis, encode it into the score (for example `score * 1e6 + (MAX_TS - ts)` within precision limits) since Redis ties sort by member name.

Related: [B2 · Locks](../academy/lessons/B2.md), [F1 · Building blocks](../academy/lessons/F1.md), [E6 · LLD case studies](../academy/lessons/E6.md).
