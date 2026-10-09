**Short answer:** Each key maps to exactly one value, so a `HashMap<Integer, Character>` (or `Map<Integer, String>`) is enough: `put` and `get` by key are O(1) on average. If you also need "which keys have value `a`?", add a reverse index `Map<Character, Set<Integer>>` and keep both maps in sync on every write. For small, dense integer keys, a plain array indexed by the key is even faster.

## Explanation

The pairs `(1,a), (2,a), (3,b)` are a many-to-one relation: keys are unique, values repeat. So:

- **Lookup by key:** `HashMap`, O(1) average. Use a `TreeMap` (O(log n)) only if you need keys in order or range queries like "keys between 10 and 20". For keys 0..N with few gaps, a `char[]` indexed by key is the fastest and uses the least memory.
- **Lookup by value:** a second map from value to the set of keys (a multimap). It costs extra memory and every write must update both maps.
- **Both directions, one-to-one only:** Guava's `BiMap`. It does **not** fit here, because `a` maps to two keys and a `BiMap` forbids duplicate values.
- **Many-to-many:** Guava `SetMultimap` or `Map<K, Set<V>>` both ways. In a database this is a join table.

## Example

```java
public final class KeyValueIndex<K, V> {
    private final Map<K, V> byKey = new HashMap<>();
    private final Map<V, Set<K>> byValue = new HashMap<>();

    public void put(K key, V value) {
        V old = byKey.put(key, value);
        if (old != null) {                                // keep the reverse index in sync
            Set<K> keys = byValue.get(old);
            keys.remove(key);
            if (keys.isEmpty()) byValue.remove(old);
        }
        byValue.computeIfAbsent(value, v -> new HashSet<>()).add(key);
    }

    public V get(K key) { return byKey.get(key); }

    public Set<K> keysFor(V value) {
        return Collections.unmodifiableSet(byValue.getOrDefault(value, Set.of()));
    }
}

var idx = new KeyValueIndex<Integer, Character>();
idx.put(1, 'a'); idx.put(2, 'a'); idx.put(3, 'b');
idx.get(2);        // 'a'
idx.keysFor('a');  // [1, 2]
```

Without a reverse index, you could build it once with streams:
`map.entrySet().stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toSet())))`.

## Pitfalls and follow-ups

- **Lookup by value as well as by key?** The reverse index above: O(1) both ways, double the memory, and every update touches both maps.
- **Why not scan `byKey.values()` for the value?** That is O(n) per query. Fine for a one-off, wrong for frequent lookups.
- **Thread safety?** `ConcurrentHashMap` makes each map safe, but the pair of maps is not updated atomically. Guard `put` with a lock, or accept brief inconsistency.
- **Memory for `Map<Integer, Character>`?** Each entry boxes the key and value and creates an entry node, so tens of bytes per pair. For millions of int keys, primitive-collection libraries (fastutil, Eclipse Collections) or an array save a lot.
- **`hashCode` and `equals`:** custom key classes must implement both consistently, and must not change while they are in the map. A record does this for you.
- **In SQL:** a table `(key PRIMARY KEY, value)` plus an index on `value` gives the same two access paths.
