Implement a priority queue of integers as a binary **min-heap**, from scratch: store the heap in an array (grow it when full) and maintain it with sift-up and sift-down. Do not use `PriorityQueue`, `TreeMap`, `TreeSet`, `Collections.sort`, `Arrays.sort` or any other library ordering structure. Implement class `MinHeap`:

- `MinHeap()` creates an empty heap.
- `void offer(int x)` inserts `x` (duplicates allowed).
- `Integer poll()` removes and returns the smallest element, or returns `null` if the heap is empty.
- `Integer peek()` returns the smallest element without removing it, or `null` if the heap is empty.
- `int size()` returns the number of elements currently stored.

**Input format**: two lines — the list of operation names, then the list of argument lists. The checker prints the list of return values, with `null` for the constructor and `offer`.

**Example 1**
Input:
["MinHeap","offer","offer","offer","peek","poll","poll","size","poll","poll"]
[[],[5],[1],[3],[],[],[],[],[],[]]
Output: [null,null,null,null,1,1,3,1,5,null]
Why: the smallest of {5,1,3} is 1; after removing 1 and 3 only 5 is left; the last poll finds the heap empty.

**Example 2**
Input:
["MinHeap","offer","offer","offer","size","poll","poll","peek"]
[[],[-2],[-2],[7],[],[],[],[]]
Output: [null,null,null,null,3,-2,-2,7]

**Constraints**
- −10⁹ ≤ x ≤ 10⁹
- at most 10⁵ calls in total

**Notes**: `offer` and `poll` should be O(log n) and `peek`/`size` O(1) — a sorted list (O(n) per insert) misses the point of the exercise. The hidden tests include 10,000 operations, duplicates, negatives and polls on an empty heap.
