**Short answer:** `keySet()`, `values()` and `entrySet()` don't copy anything. They return **views** backed by the map: live windows onto the same data. Changes to the map show up in the view immediately, and removing through the view (`remove`, `retainAll`, `removeIf`, `iterator.remove()`) removes from the map. You **can't add** through them (`UnsupportedOperationException`), and `entry.setValue()` writes through to the map. Iterate `entrySet()` when you need both key and value.

## Explanation

| View | Type | Removal writes through | `add` | Notes |
|---|---|---|---|---|
| `keySet()` | `Set<K>` | Yes | No | Removing a key removes its mapping |
| `values()` | `Collection<V>` | Yes (removes the first entry with that value) | No | Not a `Set`: values can repeat |
| `entrySet()` | `Set<Map.Entry<K,V>>` | Yes | No | `entry.setValue(v)` updates the map |

**How it is implemented.** In `HashMap`, `keySet()` returns an inner class `KeySet extends AbstractSet<K>` whose `size()` returns the map's size, whose `contains(k)` calls `containsKey(k)`, and whose iterator walks the map's buckets. The view object is created once and cached in a field, so calling `keySet()` repeatedly is cheap.

**Iteration rules.** The views' iterators are the map's iterators, so for `HashMap`/`TreeMap` they are **fail-fast**: changing the map structurally during iteration, except through the iterator, throws `ConcurrentModificationException`. For `ConcurrentHashMap` they are weakly consistent and never throw.

**Ordering** follows the map: `HashMap` has no guaranteed order, `LinkedHashMap` keeps insertion (or access) order, `TreeMap` is sorted by key.

**Related views**
- `TreeMap` / `NavigableMap`: `headMap`, `tailMap`, `subMap`, `descendingMap()` and `navigableKeySet()` are range views; inserting a key outside the range through them throws `IllegalArgumentException`.
- Java 21 **sequenced collections**: `LinkedHashMap` and `TreeMap` implement `SequencedMap`, which adds `sequencedKeySet()`, `sequencedValues()`, `sequencedEntrySet()`, `firstEntry()`, `pollFirstEntry()` and `reversed()` (a reverse-ordered view).
- `ConcurrentHashMap.keySet(defaultValue)` returns a view that **does** support `add`: it inserts the key with the given default value. `ConcurrentHashMap.newKeySet()` gives a concurrent `Set`.
- `Collections.unmodifiableMap(m)` is itself a read-only view of `m`.

## Example

```java
Map<String, Integer> stock = new HashMap<>(Map.of("apple", 5, "pear", 0, "kiwi", 3));

Set<String> keys = stock.keySet();
stock.put("mango", 7);
System.out.println(keys.contains("mango"));   // true: the view is live

keys.remove("kiwi");                           // removes the mapping from stock
stock.values().removeIf(qty -> qty == 0);     // removes "pear" from stock
// keys.add("plum");                           // UnsupportedOperationException

for (Map.Entry<String, Integer> e : stock.entrySet()) {
    e.setValue(e.getValue() * 2);              // writes through: apple=10, mango=14
}

// Remove while iterating: use the view's iterator (or removeIf)
Iterator<Map.Entry<String, Integer>> it = stock.entrySet().iterator();
while (it.hasNext()) {
    if (it.next().getValue() > 12) it.remove();   // safe; stock.remove(...) here would throw CME
}

// Need a stable copy? Copy explicitly.
List<String> snapshot = List.copyOf(stock.keySet());
```

Iterating both key and value:

```java
for (var e : map.entrySet()) use(e.getKey(), e.getValue());   // one lookup per entry
for (var k : map.keySet()) use(k, map.get(k));                // extra hash lookup per key
map.forEach((k, v) -> use(k, v));                             // also fine
```

## Pitfalls and follow-ups

- **Why can't you `add` to `keySet()`?** There is no value to pair with the key. (`ConcurrentHashMap.keySet(default)` solves this by supplying one.)
- **`values().remove(v)`** removes only the first matching entry; `values().removeAll(List.of(v))` removes all of them.
- **Storing a view in a field** keeps it tied to the map. If you return `map.keySet()` from a getter, callers can delete your entries. Return `Set.copyOf(...)` or `Collections.unmodifiableSet(...)`.
- **Entries outside iteration:** for `HashMap` the entry from `entrySet()` is the live node; for some maps (`TreeMap.firstEntry()`, `Map.entry(k, v)`) entries are immutable snapshots and `setValue` throws.
- **Mutable keys:** if a key's `hashCode` changes after insertion, `keySet().contains(key)` can return false. Keep keys immutable.
- **Performance:** views cost O(1) to obtain; `contains` on `keySet()` is O(1) for `HashMap`, but `values().contains(v)` is O(n).

Related: [H1 · Java idioms for coding interviews](../academy/lessons/H1.md), [B3 · Atomics, CAS and concurrent collections](../academy/lessons/B3.md).
