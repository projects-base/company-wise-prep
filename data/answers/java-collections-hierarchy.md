**Short answer:** `Iterable` sits at the top, then `Collection`, which splits into `List` (ordered, duplicates allowed, index access), `Set` (no duplicates) and `Queue`/`Deque` (processing order). `Map` is a separate hierarchy because it stores key-value pairs, not single elements. An array is a fixed-size language construct; a `List` is a resizable collection class with a rich API.

## Explanation

```text
Iterable
 └─ Collection
     ├─ List      → ArrayList, LinkedList, (Vector, Stack: legacy)
     ├─ Set       → HashSet, LinkedHashSet
     │   └─ SortedSet → NavigableSet → TreeSet
     └─ Queue     → PriorityQueue
         └─ Deque → ArrayDeque, LinkedList

Map (not a Collection)
 ├─ HashMap → LinkedHashMap
 ├─ SortedMap → NavigableMap → TreeMap
 └─ Hashtable (legacy), ConcurrentHashMap
```

Java 21 added `SequencedCollection` and `SequencedMap` (with `getFirst()`, `getLast()`, `reversed()`) above `List`, `Deque`, `LinkedHashSet`, `SortedSet`, `LinkedHashMap` and `SortedMap`.

**Array vs List**

| Array | List (ArrayList) |
|---|---|
| Fixed size at creation | Grows automatically (about 1.5x) |
| Holds primitives or objects | Objects only (`Integer`, autoboxing) |
| `arr.length`, `arr[i]` | `size()`, `get(i)`, `add`, `remove`, `contains` |
| Covariant (`Object[] o = new String[1]` compiles, fails at runtime) | Generics are invariant, checked at compile time |

**List vs Set:** a List keeps insertion order, allows duplicates and has index access. A Set rejects duplicates (using `equals`/`hashCode`, or `compareTo` for `TreeSet`) and has no index.

**Set vs Map:** a Set stores values; a Map stores unique keys mapped to values. In fact `HashSet` is backed by a `HashMap` whose values are a dummy object.

## Example

```java
List<String> list = new ArrayList<>(List.of("b", "a", "b"));   // [b, a, b]
Set<String> set = new HashSet<>(list);                         // [a, b] in some order
Set<String> ordered = new LinkedHashSet<>(list);               // [b, a]
Map<String, Integer> counts = new TreeMap<>();                  // sorted keys
for (String s : list) counts.merge(s, 1, Integer::sum);         // {a=1, b=2}

String[] arr = list.toArray(String[]::new);
List<String> fixed = Arrays.asList(arr);   // fixed-size view: add() throws
```

## Pitfalls and follow-ups

- **ArrayList vs LinkedList:** ArrayList is a dynamic array: O(1) `get(i)`, amortised O(1) add at the end, O(n) insert or remove in the middle (shifting). LinkedList is doubly linked: O(n) `get(i)`, O(1) add/remove only when you already hold the position (iterator or the ends). In practice ArrayList wins almost always because of cache locality; for a queue or stack use `ArrayDeque`.
- **When TreeMap?** You need keys sorted, or range queries: `floorKey`, `ceilingKey`, `headMap`, `subMap`. O(log n) per operation (red-black tree).
- **When LinkedHashMap?** You need predictable iteration order (insertion order), or access order for an LRU cache: `new LinkedHashMap<>(16, 0.75f, true)` plus overriding `removeEldestEntry`.
- **`List.of(...)` is immutable** and rejects nulls; `Arrays.asList` is fixed-size but settable.
- **Removing while iterating** with a for-each loop throws `ConcurrentModificationException`. Use `iterator.remove()` or `list.removeIf(...)`.
- **`Collection` vs `Collections`:** the interface vs the utility class (`sort`, `unmodifiableList`, `synchronizedList`).
