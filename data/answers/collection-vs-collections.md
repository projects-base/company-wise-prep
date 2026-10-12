**Short answer:** `Collection` (singular) is an **interface**: the root of the collection hierarchy that `List`, `Set` and `Queue` extend, defining operations like `add`, `remove`, `contains`, `size` and `iterator`. `Collections` (plural) is a **utility class** of static helper methods that work on collections: sorting, searching, wrapping (unmodifiable, synchronized, checked) and factory methods for empty or singleton collections. One is a type you implement or program against; the other is a toolbox you call.

## Explanation

**`java.util.Collection<E>`**
- Extends `Iterable<E>`, so every collection works in a for-each loop.
- Sub-interfaces: `List` (ordered, indexed, duplicates allowed), `Set` (no duplicates), `Queue` / `Deque` (ordered for processing). Java 21 added `SequencedCollection`, which sits between `Collection` and `List`/`Deque`/`LinkedHashSet` and gives `getFirst()`, `getLast()`, `addFirst()`, `reversed()` and so on.
- Since Java 8 it also has default methods: `stream()`, `removeIf(...)`, `spliterator()`.
- `Map` is **not** a `Collection`. It is a separate hierarchy, though its views (`keySet`, `values`, `entrySet`) are collections.

**`java.util.Collections`**
- A `final` class with a private constructor; everything is `static`. This was the standard "helpers for an interface" pattern before Java 8 allowed static methods on interfaces.
- Main groups:

| Group | Methods |
|---|---|
| Ordering | `sort(list)`, `sort(list, cmp)`, `reverse`, `shuffle`, `swap`, `rotate` |
| Searching | `binarySearch` (list must be sorted), `min`, `max`, `frequency`, `disjoint` |
| Wrappers | `unmodifiableList/Set/Map`, `synchronizedList/Set/Map`, `checkedList` |
| Factories | `emptyList()`, `singletonList(x)`, `nCopies(n, x)` |
| Bulk | `addAll(coll, a, b, c)`, `fill`, `copy` |

**Modern alternatives** (Java 9+):
- `List.of(...)`, `Set.of(...)`, `Map.of(...)` create truly immutable collections and are usually better than `Collections.unmodifiableList(new ArrayList<>(...))`.
- `List.copyOf(coll)` makes an immutable snapshot.
- `list.sort(cmp)` (Java 8) is the instance version of `Collections.sort`; `Collections.sort` now just delegates to it.

The same naming pairs exist elsewhere: `Array`/`Arrays`, `Object`/`Objects`, `Path`/`Paths`.

## Example

```java
Collection<String> names = new ArrayList<>(List.of("Rahul", "Asha", "Vikram"));
names.add("Meera");                       // Collection: instance method on the interface

List<String> list = (List<String>) names;
Collections.sort(list);                   // Collections: static helper
int idx = Collections.binarySearch(list, "Meera");

List<String> readOnly = Collections.unmodifiableList(list);
// readOnly.add("x");                     // UnsupportedOperationException
list.add("Zara");                         // but readOnly now shows "Zara": it is a view

List<String> safe = Collections.synchronizedList(new ArrayList<>());
synchronized (safe) {                     // iteration still needs manual locking
    for (String s : safe) { /* ... */ }
}

List<String> frozen = List.copyOf(list);  // a real immutable snapshot (Java 10+)
```

## Pitfalls and follow-ups

- **Is `Collections.unmodifiableList` immutable?** No. It is a read-only **view**; changes to the backing list show through. `List.copyOf` or `List.of` give real immutability.
- **`List.of` vs `Arrays.asList`:** `Arrays.asList` is fixed-size but allows `set` and writes through to the array; it allows nulls. `List.of` rejects all modification and rejects `null` elements.
- **Is `Collections.synchronizedList` enough for thread safety?** Each call is locked, but compound actions (check-then-act, iteration) still need `synchronized (list)`. For read-heavy use, `CopyOnWriteArrayList`; for maps, `ConcurrentHashMap`.
- **Why is `Map` not a `Collection`?** Its unit is a key-value pair, not an element; `add(E)` makes no sense for it.
- **What does `Collections.sort` use?** TimSort (stable, O(n log n), fast on partly sorted data) for objects; it sorts in place.
- **`Collections.emptyList()` vs `new ArrayList<>()`:** the empty list is a shared immutable singleton, so returning it avoids an allocation; callers can't add to it.

Related: [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).
