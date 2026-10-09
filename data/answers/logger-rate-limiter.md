**Short answer:** Keep a `HashMap<String, Integer>` from message to the earliest time it may be printed again. On a call, if the stored time is in the future, return `false`; otherwise store `timestamp + 10` and return `true`. Suppressed messages do not touch the map. O(1) per call. The harder "past or future" variant cannot decide at arrival time, so it buffers each message for 10 seconds before emitting it.

## Approach

- **Brute force:** keep every printed (time, message) and scan back over the last 10 seconds. O(n) per call.
- **Key insight:** for each message you only need one number: when it is next allowed. Timestamps are non-decreasing, so nothing else matters.
- **Memory:** the map grows with every distinct message ever seen. A queue of (time, message) for printed messages lets you evict entries older than 10 seconds and keep memory bounded by the last 10 seconds of traffic.

## Solution

```java
import java.util.HashMap;
import java.util.Map;

class Logger {
    private final Map<String, Integer> nextAllowed = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        Integer t = nextAllowed.get(message);
        if (t != null && timestamp < t) return false;   // still blocked
        nextAllowed.put(message, timestamp + 10);
        return true;
    }
}
```

## Complexity

- **Time:** O(1) average per call (plus O(L) to hash a message of length L).
- **Space:** O(M) for M distinct messages; O(messages in the last 10 s) with queue-based eviction.

## Edge cases

- A copy at exactly `t + 10` is printed (the check is `timestamp < t`).
- Several messages at the same timestamp: only the first copy prints.
- Different messages never block each other.
- `timestamp + 10` stays well inside `int` for timestamps up to 10⁹.

## Follow-up: suppress if a duplicate is within 10 s in the past OR the future

You cannot know about the future at arrival, so hold every message in a pending queue until 10 seconds have passed. Assumed rule: a message is printed only if no other copy arrives within 10 s on either side. When a copy arrives within 10 s of the previous copy, mark both suppressed. Release a pending message once `now >= its time + 10`.

```java
import java.util.*;

class BidirectionalLogger {
    private static final class Entry {
        final int t; final String msg; boolean suppressed;
        Entry(int t, String msg) { this.t = t; this.msg = msg; }
    }
    private final Deque<Entry> pending = new ArrayDeque<>();
    private final Map<String, Entry> last = new HashMap<>();   // latest copy per message

    /** Accepts a message and returns the messages that are now safe to print. */
    public List<String> onMessage(int t, String msg) {
        List<String> out = flush(t);
        Entry prev = last.get(msg);
        Entry e = new Entry(t, msg);
        if (prev != null && t - prev.t < 10) { prev.suppressed = true; e.suppressed = true; }
        last.put(msg, e);
        pending.addLast(e);
        return out;
    }

    public List<String> flush(int now) {
        List<String> out = new ArrayList<>();
        while (!pending.isEmpty() && pending.peekFirst().t + 10 <= now) {
            Entry e = pending.pollFirst();
            if (!e.suppressed) out.add(e.msg);
            if (last.get(e.msg) == e) last.remove(e.msg);
        }
        return out;
    }
}
```

Cost: O(1) amortised per message, but output is delayed by 10 seconds and memory holds 10 seconds of traffic. Call `flush` from a timer so messages are released even when input stops.

## Follow-up: production bottlenecks

- **Unbounded map:** millions of distinct messages leak memory; evict expired keys (queue, or a cache with TTL such as Caffeine's `expireAfterWrite`).
- **Concurrency:** many threads logging at once need `ConcurrentHashMap.compute` (atomic check-and-set per key) instead of get-then-put, which races.
- **Hashing long messages:** hash a normalised template, not the full text with IDs and timestamps inside, or nothing will ever be a duplicate.
- **Multiple instances:** each JVM has its own map; global dedup needs a shared store (for example Redis `SET key NX EX 10`), which adds a network hop per log line.
- **Clock skew and out-of-order timestamps** across producers break the "non-decreasing" assumption.

## Follow-up: should suppressed messages update the timestamp?

It is a product decision. Not updating (this solution) gives at most one print per 10 s, so a constant stream still shows up every 10 s: good for visibility. Updating turns it into a "quiet period" rule: a message that repeats every 5 s is printed once and never again until it stops, which can hide an ongoing problem. Most log throttles do not update, and many also report "suppressed N duplicates".

See [B7 · Classic problems: rate limiter](../academy/lessons/B7.md).

Practise it in the app: Run / Submit on this page.
