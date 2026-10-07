Design a structure that tracks which real numbers on a line are currently covered. All ranges are half-open intervals `[left, right)`, containing every real `x` with `left ≤ x < right`.

- `RangeModule()` starts with nothing tracked.
- `void addRange(int left, int right)` starts tracking every number in `[left, right)`. Parts that were already tracked stay tracked.
- `boolean queryRange(int left, int right)` returns `true` only if **every** number in `[left, right)` is currently tracked.
- `void removeRange(int left, int right)` stops tracking every number in `[left, right)`.

**Input format**: the first line lists the operations, the second line their arguments. The output is the list of results (`null` for the constructor and for void calls).

**Example 1**
Input:
["RangeModule","addRange","removeRange","queryRange","queryRange","queryRange"]
[[],[10,20],[14,16],[10,14],[13,15],[16,17]]
Output: [null,null,null,true,false,true]
Why: after the calls, [10,14) and [16,20) are tracked. [13,15) includes 14, which is not tracked.

**Example 2**
Input:
["RangeModule","addRange","addRange","queryRange","removeRange","queryRange"]
[[],[1,5],[5,9],[2,8],[3,4],[1,3]]
Output: [null,null,null,true,null,true]
Why: [1,5) and [5,9) join into [1,9). Removing [3,4) leaves [1,3) intact.

**Constraints**
- 1 ≤ left < right ≤ 10⁹
- at most 10⁴ operations in total

**Notes**: an early version of this question asked only about single points. A point query for `x` is the same as `queryRange(x, x + 1)`. Keep the tracked set as sorted, disjoint intervals; a boolean per number is impossible at this scale.
