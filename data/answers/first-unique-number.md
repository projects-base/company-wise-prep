**Short answer:** Keep a count per number and a FIFO queue of numbers in the order they first arrived. `add` bumps the count and enqueues the number only on its first arrival. `showFirstUnique` lazily pops queue heads whose count is now above 1, then returns the head. Each number is enqueued once and dequeued at most once, so both operations are O(1) amortised.

## Picture it

Example 1: start with `[2, 3, 5]`, then add 5, 2, 3, asking for the first unique number between adds.

| Call | count after | queue (head on the left) | Lazy pops | Returns |
|---|---|---|---|---|
| constructor [2,3,5] | 2:1, 3:1, 5:1 | [2, 3, 5] | — | — |
| showFirstUnique | same | [2, 3, 5] | none (count[2] = 1) | 2 |
| add(5) | 5:2 | [2, 3, 5] (not re-queued) | — | — |
| showFirstUnique | same | [2, 3, 5] | none | 2 |
| add(2) | 2:2 | [2, 3, 5] | — | — |
| showFirstUnique | same | [3, 5] | pop 2 (count 2) | 3 |
| add(3) | 3:2 | [3, 5] | — | — |
| showFirstUnique | same | [] | pop 3, pop 5 | -1 |

5 became a duplicate early but stayed in the queue until it reached the head. That is the lazy part.

**The picture in one sentence:** a queue of first arrivals plus counts, where stale heads are thrown away only when they block the answer, and a duplicate can never come back.

## Approach

- **Brute force:** keep the full arrival list and, on every query, scan it for the first number with count 1. O(n) per query.
- **Key insight:** once a number becomes a duplicate it can never be unique again. So a stale entry at the front of the queue can be thrown away for good. That makes lazy deletion safe and cheap.
- **Optimal:** `HashMap<Integer, Integer>` for counts plus an `ArrayDeque` for first arrivals. An eager alternative is a `LinkedHashSet` of currently-unique numbers plus a set of seen numbers: remove from the linked set on the second arrival, and the first element of the set is the answer. Both are O(1).

## Solution

```java
import java.util.*;

class FirstUnique {
    private final Map<Integer, Integer> count = new HashMap<>();
    private final ArrayDeque<Integer> queue = new ArrayDeque<>(); // first arrivals, in order

    public FirstUnique(int[] nums) {
        for (int x : nums) add(x);
    }

    public int showFirstUnique() {
        // lazily drop numbers that have become duplicates
        while (!queue.isEmpty() && count.get(queue.peek()) > 1) queue.poll();
        return queue.isEmpty() ? -1 : queue.peek();
    }

    public void add(int value) {
        int c = count.merge(value, 1, Integer::sum);
        if (c == 1) queue.add(value);
    }
}
```

## Complexity

- **Time:** `add` O(1); `showFirstUnique` O(1) amortised, because the total number of pops over the object's life is at most the number of distinct values. See [C5 · Amortised analysis](../academy/lessons/C5.md).
- **Space O(d)** for `d` distinct numbers.

## Edge cases

- Everything duplicated: return `-1` (Example 2 before `8` arrives).
- A number arriving a third time: count goes to 3, still not unique, no new queue entry.
- Empty starting array: constructor adds nothing.

## Follow-ups

- **Millions of concurrent callers.** The simple fix is to make both methods `synchronized`, which is correct because the count update and the enqueue must be atomic together. To scale, use a `ConcurrentHashMap` with `merge` (atomic per key) for counts and a `ConcurrentLinkedQueue` for first arrivals. Only the thread whose `merge` returned 1 enqueues, so each number is queued once. Readers peek the head and `poll` it only if its count is above 1; a lost race on `poll` is harmless because the next reader re-checks. The answer is "eventually consistent": a reader may briefly see a number that has just become a duplicate, which is usually acceptable for a gallery.
- **Synchronisation serialises consumers; how to go faster?** Separate reads from writes. Readers do not need a lock at all with the lock-free design above. If you keep a lock, use a `ReadWriteLock` or `StampedLock` optimistic read for `showFirstUnique`, and let a single background thread do the queue clean-up. If consumers actually *take* paintings (remove them), shard by number into independent queues and have each consumer take the oldest head across shards, or accept approximate ordering per shard. Batch the writes (buffer adds per thread, apply in bulk) to cut contention on hot keys. Discuss in terms of [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md) and [B7 · Classic problems](../academy/lessons/B7.md).

Practise it in the app: Run / Submit on this page.
