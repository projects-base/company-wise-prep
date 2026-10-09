**Short answer:** A `HashMap` is an array of buckets. `put` and `get` compute the key's `hashCode()`, mix it, pick a bucket with `hash & (n - 1)`, then walk that bucket comparing with `equals()`. With a good hash spread, insert, delete and get are O(1) on average. A bucket that grows long (8+ entries, table of at least 64) turns into a red-black tree, so the worst case is O(log n) instead of O(n).

## Explanation

- **Structure:** `Node<K,V>[] table`, each node holding `hash, key, value, next`. Default capacity 16, load factor 0.75. Capacity is always a power of two, so `hash & (n - 1)` is a fast modulo.
- **Hash spreading:** `hash = h ^ (h >>> 16)`. It mixes the high bits into the low bits, because only the low bits pick the bucket.
- **put:** find the bucket. If empty, place the node. Otherwise walk the chain: same hash and `equals` → replace the value; else append at the tail. Then `++size`; if `size > capacity * loadFactor`, resize.
- **Resize:** double the array and redistribute. Because capacity doubles, each node either stays at index `i` or moves to `i + oldCap`, decided by one bit. Resize is O(n), but rare, so `put` is amortised O(1).
- **Treeify (Java 8+):** a bin becomes a red-black tree when it reaches 8 nodes (`TREEIFY_THRESHOLD`), but only if the table has at least 64 slots; below that, the map resizes instead. A tree bin turns back into a list when it gets small again (6 or fewer nodes after a resize split).
- **null:** one null key is allowed (it hashes to bucket 0) and any number of null values.

| Operation | Average | Worst (Java 8+) |
|---|---|---|
| `get` / `containsKey` | O(1) | O(log n) per tree bin |
| `put` | O(1) amortised | O(log n), plus O(n) on a resize |
| `remove` | O(1) | O(log n) |
| iterate | O(capacity + size) | |

## Example

```java
record Point(int x, int y) {}          // records give correct equals/hashCode

Map<Point, String> grid = new HashMap<>(64);   // presize if you know the count
grid.put(new Point(1, 2), "A");
grid.get(new Point(1, 2));             // "A": equal key, same bucket

// Broken key: equals overridden, hashCode not
class BadKey {
    int id;
    @Override public boolean equals(Object o) { return o instanceof BadKey b && b.id == id; }
}
// two equal BadKeys usually land in different buckets → get() returns null
```

## Pitfalls and follow-ups

- **Why must keys be immutable?** If a field used in `hashCode` changes after `put`, the entry sits in the wrong bucket and `get` can't find it.
- **What is a collision?** Two keys in the same bucket. Different keys can share a hash; `equals` tells them apart.
- **Why is the tree threshold 8?** With a decent hash, a chain of 8 is very unlikely; the tree is a defence against poor or hostile hashes, not the normal path.
- **Tree bins need ordering:** they order by hash, then by `compareTo` if keys are `Comparable`, then a tie-break. `Comparable` keys keep the tree efficient.
- **Is it thread-safe?** No. Concurrent `put`s can lose updates. Use `ConcurrentHashMap`.
- **Iteration order** is not guaranteed. Use `LinkedHashMap` for insertion order, `TreeMap` for sorted order.
- **Initial capacity for 1,000 entries?** `1000 / 0.75 ≈ 1334`, rounded up to 2048. Java 19+ has `HashMap.newHashMap(1000)` which does this maths for you.

Related: [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md).
