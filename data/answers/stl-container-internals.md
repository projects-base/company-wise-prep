**Short answer:** `vector` is a contiguous dynamic array (three pointers: begin, end, end of capacity) that grows geometrically. `deque` is a map of fixed-size blocks, so it grows at both ends without moving elements. `list` is a doubly linked list of nodes. `map`/`set` are red-black trees (balanced binary search trees, O(log n), sorted). `unordered_map`/`unordered_set` are hash tables with separate chaining: an array of buckets, each a linked list of nodes, rehashing when the load factor passes 1.0 by default. `priority_queue` is a binary heap on top of a `vector`. The standard specifies complexity and iterator rules, and those rules force these designs.

## Explanation

| Container | Structure | Key operations | Iterator / reference invalidation |
|---|---|---|---|
| `vector` | contiguous array | random access O(1), `push_back` amortised O(1), middle insert O(n) | reallocation invalidates all; insert/erase invalidates from that point |
| `deque` | array of pointers to fixed blocks | O(1) at both ends, random access O(1) (two indirections) | insert at ends invalidates iterators but **not** references |
| `list` | doubly linked nodes | O(1) insert/erase anywhere given an iterator, `splice` | only erased element |
| `map`, `set` | red-black tree | O(log n) find/insert/erase, in-order iteration | only erased element |
| `unordered_map` | bucket array + node chains | average O(1), worst O(n) | rehash invalidates iterators, not references |
| `priority_queue` | binary heap in a `vector` | push/pop O(log n), top O(1) | no iterators |
| `array` | fixed C array | no allocation | n/a |

**Why these designs**
- `map` must iterate in order and give O(log n) worst case; red-black trees rebalance with at most a few rotations per insert, cheaper than AVL for write-heavy use.
- `unordered_map` must keep references stable across rehash and support the bucket API (`bucket_count`, `bucket(key)`), which effectively requires node-based chaining. That costs one allocation per element and pointer chasing, which is why open-addressing tables (Abseil `flat_hash_map`, `boost::unordered_flat_map`) are faster in latency-sensitive code.
- `vector` growth by a constant factor (2x in libstdc++/libc++, 1.5x in MSVC) gives amortised O(1) `push_back`.

**Memory and cache behaviour** matter more than big-O for small n: a `vector` scan beats `list` and `map` because of contiguous memory and prefetching. Node containers do one heap allocation per element.

## Example

```cpp
std::unordered_map<std::string, int> m;
m.reserve(10'000);              // avoid rehashes: buckets >= 10000 / max_load_factor
m.max_load_factor(0.7f);

std::map<int, Order> book;      // price -> level, sorted
auto it = book.lower_bound(100);   // O(log n), first price >= 100

std::vector<int> v;
v.reserve(1'000);               // one allocation
std::priority_queue<int, std::vector<int>, std::greater<>> minHeap;
```

## Pitfalls and follow-ups

- **Why is `map` a red-black tree and not a B-tree?** The standard's iterator and reference stability rules suit a node-per-element tree; B-tree maps exist outside the standard (e.g. Abseil `btree_map`) and are more cache-friendly.
- **Worst case of `unordered_map`?** O(n) when many keys collide (bad hash or adversarial input).
- **`std::string` internals:** small string optimisation stores short strings (about 15 chars in libstdc++, 22 in libc++ on 64-bit) inline with no allocation.
- **`vector<bool>`** is a bit-packed specialisation, not a real container of `bool`.
- **Java comparison:** `ArrayList` ~ `vector`, `ArrayDeque` ~ `deque` (but a circular array), `TreeMap` ~ `map` (also red-black), `HashMap` ~ `unordered_map` (chaining, but since Java 8 a bucket with 8+ entries becomes a tree once the table has at least 64 buckets).
