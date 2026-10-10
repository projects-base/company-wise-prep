**Short answer:** Keep three maps: key → value, key → use count, and count → keys with that count in recency order (a `LinkedHashSet`, oldest first). Track `minCount`. On a use, move the key from bucket c to bucket c + 1, and bump `minCount` if bucket c was the minimum and is now empty. To evict, take the first key of the `minCount` bucket. A new key always resets `minCount` to 1. Every step is O(1) on average.

## Picture it

Example 1, `capacity = 2`. Buckets map a use count to its keys, oldest first.

| Call | Evicts | buckets after (count: keys) | minCount | Returns |
|---|---|---|---|---|
| put(1,1) | — | 1: [1] | 1 | — |
| put(2,2) | — | 1: [1, 2] | 1 | — |
| get(1) | — | 1: [2], 2: [1] | 1 | 1 |
| put(3,3) | 2 (first of bucket 1) | 1: [3], 2: [1] | 1 | — |
| get(2) | — | unchanged | 1 | -1 |
| get(3) | — | 2: [1, 3] (bucket 1 emptied) | 2 | 3 |
| put(4,4) | 1 (first of bucket 2, older than 3) | 1: [4], 2: [3] | 1 | — |
| get(1) | — | unchanged | 1 | -1 |
| get(3) | — | 1: [4], 3: [3] (bucket 2 emptied, but min is 1) | 1 | 3 |
| get(4) | — | 2: [4], 3: [3] (bucket 1 emptied) | 2 | 4 |

**The picture in one sentence:** counts only climb one bucket at a time, so the victim is always the oldest key in the `minCount` bucket and `minCount` never needs a search.

## Approach

- **Brute force:** store counts and last-use times, and scan all entries to find the victim on eviction. O(capacity) per `put`.
- **Heap:** a priority queue ordered by (count, lastUse). Eviction is O(log n), but every `get` changes a key's priority, which needs a remove-and-reinsert: O(log n), or O(n) with `PriorityQueue.remove(Object)`.
- **Key insight:** counts only ever go up by exactly 1. So a key moves from one bucket to the next one, and the minimum count can only (a) stay, (b) go up by one when its bucket empties because of a touch, or (c) drop to 1 when a new key arrives. That makes `minCount` easy to maintain without a heap. Inside a bucket, `LinkedHashSet` keeps insertion order, which is exactly "least recently used first" for keys with equal counts.

## Solution

```java
import java.util.*;

class LFUCache {
    private final int capacity;
    private final Map<Integer, Integer> values = new HashMap<>();
    private final Map<Integer, Integer> counts = new HashMap<>();
    // use count -> keys with that count, least recently used first
    private final Map<Integer, LinkedHashSet<Integer>> buckets = new HashMap<>();
    private int minCount = 0;

    public LFUCache(int capacity) { this.capacity = capacity; }

    public int get(int key) {
        Integer v = values.get(key);
        if (v == null) return -1;
        touch(key);
        return v;
    }

    public void put(int key, int value) {
        if (capacity <= 0) return;
        if (values.containsKey(key)) {
            values.put(key, value);
            touch(key);
            return;
        }
        if (values.size() >= capacity) {
            LinkedHashSet<Integer> bucket = buckets.get(minCount);
            int victim = bucket.iterator().next();      // oldest among least used
            bucket.remove(victim);
            if (bucket.isEmpty()) buckets.remove(minCount);
            values.remove(victim);
            counts.remove(victim);
        }
        values.put(key, value);
        counts.put(key, 1);
        buckets.computeIfAbsent(1, c -> new LinkedHashSet<>()).add(key);
        minCount = 1;
    }

    private void touch(int key) {
        int c = counts.get(key);
        LinkedHashSet<Integer> bucket = buckets.get(c);
        bucket.remove(key);
        if (bucket.isEmpty()) {
            buckets.remove(c);
            if (minCount == c) minCount = c + 1;
        }
        counts.put(key, c + 1);
        buckets.computeIfAbsent(c + 1, x -> new LinkedHashSet<>()).add(key);
    }
}
```

## Complexity

- **Time:** O(1) average for `get` and `put`: a constant number of hash operations.
- **Space:** O(capacity).

## Edge cases

- Capacity 0 (if allowed): `put` does nothing, `get` returns −1.
- `put` on an existing key updates the value *and* counts as a use; it never evicts.
- Evict before inserting, and only when the key is new.
- Ties on count: evict the least recently used in that bucket.
- A full cache where every key has a high count: the new key still enters with count 1 and becomes the next victim. That is how LFU behaves.

## The score-based variant from the interview

The reported version: each entry has a score that increments on access, and eviction follows LFU order but may only evict entries whose score is currently even. Clarify first what to do if no even entry exists (reject the insert, or fall back to plain LFU).

The clean O(1) way: since every access adds 1, every access flips parity. Keep **two** bucket structures, one for even-score keys and one for odd-score keys, each with its own `minCount`. A touch moves the key from one structure to the other. Eviction looks only at the even structure. A naive version that walks LFU order and skips odd entries works but is O(n) per eviction; say so, then offer the two-structure version.

## Follow-up: "handle edge cases and use classes cleanly"

The interviewer feedback suggests modelling it with types, not parallel maps: a `Node` class (key, value, count, prev, next), a `DoublyLinkedList` per count with O(1) `remove` and `addLast`, and `LFUCache<K, V>` generic over key and value. Validate capacity in the constructor, keep `touch` private, and keep each method short. The hand-rolled list avoids boxing and makes the O(1) removal obvious.

See [B7 · Classic problems: concurrent LRU, rate limiter](../academy/lessons/B7.md) for making a cache like this thread-safe.

Practise it in the app: Run / Submit on this page.
