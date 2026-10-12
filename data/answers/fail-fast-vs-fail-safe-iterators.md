**Short answer:** A **fail-fast** iterator (from `ArrayList`, `HashMap`, `HashSet`) throws `ConcurrentModificationException` as soon as it notices the collection was structurally changed by anything other than the iterator itself. A **fail-safe** (better called *non-failing* or *weakly consistent*) iterator never throws that exception: `CopyOnWriteArrayList` iterates over a snapshot, and `ConcurrentHashMap` iterates over the live table and may or may not show concurrent changes. Fail-fast is a bug detector, not a thread-safety mechanism.

## Explanation

**How fail-fast works**
- The collection keeps an `int modCount`, incremented on every **structural** change (add, remove, clear, resize). Replacing a value with `set` or `map.put` on an existing key is not structural.
- The iterator copies it into `expectedModCount` when created. Each `next()` (and `remove()`) checks `modCount == expectedModCount`; a mismatch throws `ConcurrentModificationException`.
- `iterator.remove()` updates both counts, which is why removing *through* the iterator is allowed.
- It is **best effort**. `modCount` is not volatile and there is no locking, so with real concurrent threads the check can miss a change. The Javadoc says programs must not depend on the exception for correctness.
- It fires in a **single thread** too. The classic case is `list.remove(x)` inside a for-each loop.

**Fail-safe / weakly consistent iterators** (`java.util.concurrent`)

| Collection | Iterator behaviour | Cost |
|---|---|---|
| `CopyOnWriteArrayList` / `CopyOnWriteArraySet` | Snapshot of the array at creation; never sees later changes; `iterator.remove()` unsupported | Every write copies the whole array |
| `ConcurrentHashMap` | Weakly consistent: traverses the live table, reflects some changes made after creation, never throws CME, never returns an element twice | Little; no copying |
| `ConcurrentLinkedQueue`, `ConcurrentSkipListMap` | Weakly consistent | Little |

"Fail-safe" is interview vocabulary, not an official Java term. The Javadoc uses **weakly consistent** and **snapshot**.

## Example

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));

for (Integer n : nums) {            // fail-fast
    if (n % 2 == 0) nums.remove(n); // ConcurrentModificationException on the next next()
}

// Correct ways in a single thread:
Iterator<Integer> it = nums.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) it.remove();   // remove through the iterator
}
nums.removeIf(n -> n % 2 == 0);            // simplest (Java 8+)

// Snapshot iteration:
List<Integer> cow = new CopyOnWriteArrayList<>(List.of(1, 2, 3));
for (Integer n : cow) {
    cow.add(n * 10);                // no exception; the loop sees only 1, 2, 3
}
// cow = [1, 2, 3, 10, 20, 30]

// Weakly consistent:
Map<String, Integer> m = new ConcurrentHashMap<>(Map.of("a", 1, "b", 2));
for (String k : m.keySet()) {
    m.remove(k);                    // allowed, no exception
}
```

## Pitfalls and follow-ups

- **The "second-to-last element" surprise:** removing the second-to-last element inside a for-each loop over an `ArrayList` does *not* throw. After the removal `hasNext()` checks `cursor != size`, returns false, and the loop ends before `next()` runs the check. The last element is silently never visited. A good sign you understand the mechanism.
- **Does fail-fast make a collection thread-safe?** No. It only detects some misuse. Use a concurrent collection or a lock.
- **Why not make every iterator a snapshot?** Copying costs O(n) memory and time per write (for COW) and gives stale reads. COW fits read-mostly lists such as listener registries.
- **`Collections.synchronizedList` iterator:** fail-fast, and you must hold the list's lock while iterating: `synchronized (list) { for (...) }`.
- **Does `ConcurrentHashMap.size()` give an exact count during updates?** No, it is an estimate under concurrency. `mappingCount()` returns a `long` and is preferred for large maps.
- **Streams:** modifying the source during a stream pipeline gives undefined results and often CME; streams are fail-fast too for `ArrayList` sources.

Related: [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md), [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).
