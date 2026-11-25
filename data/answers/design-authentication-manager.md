**Short answer:** Every token lives equally long and time only moves forward, so tokens expire in the order they were generated or renewed. Keep a map from token to expiry, and a queue of (token, expiry) with the oldest first. Before each call, pop expired entries from the front and remove a token from the map only if its stored expiry is still the popped one. The count is then `map.size()`, and every operation is amortised O(1).

## Picture it

`timeToLive = 5`. Queue entries are `token@expiry`, oldest on the left.

| Call | Evict first (expiry <= now) | Queue after | Map after | Result |
|---|---|---|---|---|
| `renew(aaa, 1)` | nothing | empty | empty | ignored, unknown |
| `generate(aaa, 2)` | nothing | `aaa@7` | aaa: 7 | |
| `count(6)` | nothing, 7 > 6 | `aaa@7` | aaa: 7 | **1** |
| `generate(bbb, 7)` | `aaa@7` (7 <= 7) | `bbb@12` | bbb: 12 | |
| `renew(aaa, 8)` | nothing | `bbb@12` | bbb: 12 | ignored, expired |
| `renew(bbb, 10)` | nothing | `bbb@12`, `bbb@15` | bbb: 15 | stale `bbb@12` stays |
| `count(15)` | `bbb@12`: map says 15, skip. `bbb@15`: remove | empty | empty | **0** |

**The picture in one sentence:** a renew does not move anything, it appends a newer entry and lets the old one die harmlessly at the front.

## Approach

- **Limits:** LeetCode 1797 allows 2,000 calls, so anything passes there. The reported round asked for 10^5.
- **Brute force.** Map of token to expiry; `count` scans it. O(n) per count, so up to about 2.5 billion checks for 10^5 mixed calls.
- **TreeMap by expiry.** `tailMap(now, false).size()` looks like O(log n), but Java counts a submap by **walking it**. That is the reported time limit failure.
- **Measured here** (Java 21, one laptop, 50,000 generates and 50,000 counts, nothing expiring): TreeMap submap about 50 s, queue about 65 ms.
- **Key insight.** Fixed TTL plus non-decreasing time means new expiries are never smaller than old ones. The data arrives already sorted, so a plain queue replaces the tree.

## Solution

```java
import java.util.*;

class AuthenticationManager {
    private record Expiry(String tokenId, int at) {}

    private final int timeToLive;
    private final Map<String, Integer> expiresAt = new HashMap<>();   // live tokens only, after evict()
    private final Deque<Expiry> queue = new ArrayDeque<>();           // oldest expiry first; may hold stale entries

    public AuthenticationManager(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void generate(String tokenId, int currentTime) {
        evict(currentTime);
        schedule(tokenId, currentTime);
    }

    public void renew(String tokenId, int currentTime) {
        evict(currentTime);
        if (expiresAt.containsKey(tokenId)) schedule(tokenId, currentTime);
    }

    public int countUnexpiredTokens(int currentTime) {
        evict(currentTime);
        return expiresAt.size();
    }

    private void schedule(String tokenId, int currentTime) {
        int expiry = currentTime + timeToLive;
        expiresAt.put(tokenId, expiry);
        queue.addLast(new Expiry(tokenId, expiry));
    }

    private void evict(int currentTime) {
        while (!queue.isEmpty() && queue.peekFirst().at() <= currentTime) {   // expiring exactly now counts as expired
            Expiry head = queue.pollFirst();
            expiresAt.remove(head.tokenId(), head.at());                      // no-op if a renew moved the expiry on
        }
    }
}
```

Without stale entries, using `LinkedHashMap` order as the queue:

```java
import java.util.*;

class LinkedAuthenticationManager {
    private final int timeToLive;
    private final LinkedHashMap<String, Integer> expiresAt = new LinkedHashMap<>();   // insertion order = expiry order

    LinkedAuthenticationManager(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void generate(String tokenId, int currentTime) {
        evict(currentTime);
        moveToEnd(tokenId, currentTime);
    }

    public void renew(String tokenId, int currentTime) {
        evict(currentTime);
        if (expiresAt.containsKey(tokenId)) moveToEnd(tokenId, currentTime);
    }

    public int countUnexpiredTokens(int currentTime) {
        evict(currentTime);
        return expiresAt.size();
    }

    private void moveToEnd(String tokenId, int currentTime) {
        expiresAt.remove(tokenId);                        // put alone would keep the old position
        expiresAt.put(tokenId, currentTime + timeToLive);
    }

    private void evict(int currentTime) {
        Iterator<Integer> oldest = expiresAt.values().iterator();
        while (oldest.hasNext() && oldest.next() <= currentTime) oldest.remove();
    }
}
```

## Complexity

- **Time:** amortised O(1) per call. Each generate or renew adds one queue entry and each entry is popped once, so n calls together are O(n).
- **Worst single call:** O(n), when everything expires at once.
- **Space:** queue version O(entries added in the last TTL), including stale ones. `LinkedHashMap` version O(live tokens).
- Prefer the second when clients renew often: a token renewed 1,000 times in one TTL leaves 1,000 queue entries in the first.

## Edge cases

- **Expiry exactly now counts as expired**, and that check comes before the other action. Use `<=`.
- Renew of an unknown or expired token does nothing.
- Renew several times: only the last expiry counts (`map.remove(key, value)` handles it).
- No calls for a long time, then one: a single call pops everything. Fine, and it happens once.
- `currentTime + timeToLive` is safe within LeetCode's limits (each at most 10^8). Use `long` for real clocks.

## Follow-ups

**Make it pass with 10^5 queries.** Either version above. Say why the submap size is linear, and that a hand-written linked list gives the same bound as `LinkedHashMap` with more code.

**Clean API, boundaries, tests, performance.** In the open-ended variant I would change the API, not only the algorithm:

- The caller should not pass the time. Inject a `Clock`, so nobody can send a wrong one and tests do not sleep.
- `renew` returns a boolean, so the caller can ask the user to log in.
- `generate` refuses a live id. In a real system the server makes the id itself with `SecureRandom`.
- Guard the clock: wall time can step back, and that would break the sorted order.

```java
import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

/** Thread-safe, in memory, one node. */
public final class SessionTokens {
    private final long ttlMillis;
    private final Clock clock;
    private final LinkedHashMap<String, Long> expiresAt = new LinkedHashMap<>();
    private long lastNow = Long.MIN_VALUE;

    public SessionTokens(Duration ttl, Clock clock) {
        if (ttl.toMillis() <= 0) throw new IllegalArgumentException("ttl must be at least 1 ms");
        this.ttlMillis = ttl.toMillis();
        this.clock = Objects.requireNonNull(clock);
    }

    /** Throws if the token is still live: two sessions must never share an id. */
    public synchronized void generate(String tokenId) {
        requireId(tokenId);
        long now = evict();
        if (expiresAt.containsKey(tokenId)) throw new IllegalStateException("token is already active");
        expiresAt.put(tokenId, now + ttlMillis);
    }

    /** Returns false if the token is unknown or expired, so the caller can send the user to log in. */
    public synchronized boolean renew(String tokenId) {
        requireId(tokenId);
        long now = evict();
        if (expiresAt.remove(tokenId) == null) return false;
        expiresAt.put(tokenId, now + ttlMillis);
        return true;
    }

    public synchronized int activeCount() {
        evict();
        return expiresAt.size();
    }

    private long evict() {
        lastNow = Math.max(lastNow, clock.millis());      // never let a clock step backwards break the expiry order
        Iterator<Long> oldest = expiresAt.values().iterator();
        while (oldest.hasNext() && oldest.next() <= lastNow) oldest.remove();
        return lastNow;
    }

    private static void requireId(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) throw new IllegalArgumentException("tokenId is required");
    }
}
```

**Tests to write:**

```java
@Test
void tokenIsLiveJustBeforeItsExpiryAndGoneExactlyAtIt() {
    tokens.generate("a");                                 // ttl 5 ms, test clock at 0
    clock.advance(4);
    assertEquals(1, tokens.activeCount());
    clock.advance(1);
    assertEquals(0, tokens.activeCount());
    assertFalse(tokens.renew("a"));
}
```

- Renew at 4 moves the expiry to 9: alive at 8, gone at 9.
- Renew of an unknown id; generate of a live id; generate again after expiry.
- Clock goes backwards; blank id; zero TTL.
- 10,000 generates from 8 threads lose nothing.
- Random calls compared with the brute force: the best single test for this kind of class.

## Variations

- **Different TTL per token:** arrival order is no longer expiry order. Use a `PriorityQueue` by expiry with the same lazy check, O(log n).
- **Calls out of time order:** the trick fails. Use a Fenwick tree over expiry times, or sort the calls if you may answer offline.
- **Count at a past time:** keep all expiries in a sorted list and binary search.
- **Many nodes:** Redis `SET token user PX ttl`, renew with `PEXPIRE ... XX` (Redis 7+). For a live count use a sorted set scored by expiry with `ZCOUNT`.
- **Real login sessions:** store a hash of the token, cap the total lifetime as well as the idle time, and support revoke.

Go deeper: [J6 · Collections internals](../academy/lessons/J6.md), [T1 · Hashing, two pointers, sliding window and prefix sums](../academy/lessons/T1.md).
