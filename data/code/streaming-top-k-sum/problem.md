Design a structure that keeps a changing collection of items, each with a unique integer `id` and an integer value, and can report the sum of the `k` largest values at any moment.

- `TopKSum(int k)` creates an empty structure.
- `void upsert(int id, int value)`: if `id` is not present, add it with this value; if it is present, change its value.
- `void remove(int id)`: delete the item `id`. If it is not present, do nothing.
- `long topKSum()`: return the sum of the `k` largest values currently stored. If fewer than `k` items are stored, return the sum of all of them (0 when empty).

Every operation should run in O(log n).

**How the tests work**: the first line lists the operations and the second their arguments, as on LeetCode. The judge prints one result per operation, with `null` for the constructor and for void methods.

**Example 1**
Input:
["TopKSum","upsert","upsert","upsert","topKSum","upsert","topKSum","remove","topKSum"]
[[2],[1,5],[2,3],[3,8],[],[2,10],[],[3],[]]
Output: [null,null,null,null,13,null,18,null,15]
Why: values {5,3,8} give 8+5 = 13. Item 2 becomes 10, so {5,10,8} give 18. Removing item 3 leaves {5,10}, which sum to 15.

**Example 2**
Input:
["TopKSum","topKSum","upsert","topKSum","remove","remove","topKSum"]
[[3],[],[7,-4],[],[7],[7],[]]
Output: [null,0,null,-4,null,null,0]
Why: with fewer than k items everything is summed; removing a missing id does nothing.

**Constraints**
- 1 ≤ k ≤ 10⁵
- 0 ≤ id ≤ 10⁹, −10⁹ ≤ value ≤ 10⁹
- at most 10⁵ operations in total
- sums can exceed the range of `int`; return a `long`
