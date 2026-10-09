**Short answer:** `HashMap` is not thread-safe, fast, and allows one null key and null values. `Hashtable` is a legacy class where every method is `synchronized` on the whole map, so only one thread works at a time, and it allows no nulls. `ConcurrentHashMap` is the modern thread-safe map: lock-free reads, and writes lock only the single bucket they touch, so many threads can write at once. It also forbids nulls.

## Explanation

| | HashMap | Hashtable | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | No | Yes, one lock for the whole map | Yes, per-bucket locking + CAS |
| Null key / values | 1 null key, null values | Neither | Neither |
| Iterator | Fail-fast | Fail-fast (its `Enumeration` is not) | Weakly consistent (fail-safe) |
| Since | 1.2 | 1.0 (legacy) | 1.5, rewritten in 8 |
| Use today | Single-thread or confined | Don't | Shared mutable maps |

**How ConcurrentHashMap works in Java 8+ (follow-up):**

- Same idea as `HashMap`: an array of bins, lists that treeify when long.
- **Reads** (`get`) take no lock. Node values and `next` pointers are `volatile`, so a reader sees a consistent view.
- **Insert into an empty bin** uses a CAS (compare-and-swap) to place the first node, no lock.
- **Insert into a non-empty bin** does `synchronized` on the first node of that bin only. Threads writing to different bins never block each other.
- **Resize** is cooperative: threads that arrive during a resize help move bins.
- **Size** is tracked with striped counters (like `LongAdder`), so `size()` is an estimate under concurrent updates.
- Java 7 used a different design: 16 `Segment`s, each a `ReentrantLock`. Java 8 dropped segments for finer per-bin locking.

**Why no nulls in ConcurrentHashMap?** `get(key)` returning `null` must mean "absent". With nulls allowed, a thread could not tell "absent" from "mapped to null" without a second call, and between the two calls another thread could change the map.

## Example

```java
Map<String, Integer> hits = new ConcurrentHashMap<>();

// Wrong: check-then-act race, even on a concurrent map
if (!hits.containsKey(page)) hits.put(page, 0);
hits.put(page, hits.get(page) + 1);

// Right: single atomic operations
hits.merge(page, 1, Integer::sum);
hits.computeIfAbsent(user, u -> new ConcurrentLinkedQueue<>()).add(event);
```

## Pitfalls and follow-ups

- **Fail-fast vs fail-safe:** fail-fast iterators (`HashMap`, `ArrayList`) check a `modCount` and throw `ConcurrentModificationException` if the collection changes structurally during iteration (except through the iterator's own `remove`). It is best-effort, not a guarantee. Fail-safe iterators (`ConcurrentHashMap`, `CopyOnWriteArrayList`) never throw; `ConcurrentHashMap`'s iterator is weakly consistent: it may or may not show changes made after it was created.
- **`Collections.synchronizedMap(map)`** behaves like `Hashtable`: one lock, and you must synchronize manually while iterating.
- **Individual calls are atomic, sequences are not.** `get` then `put` is still a race. Use `compute`, `merge`, `putIfAbsent`.
- **Don't do slow work inside `computeIfAbsent`.** The bin stays locked while the function runs, and the function must not modify the same map.
- **Why is Hashtable slow?** Every read and write takes the same monitor, so threads queue even for reads.

Deeper: [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md).
