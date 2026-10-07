You are given a list of meetings, where `intervals[i] = [start, end]` means meeting `i` runs from time `start` up to (but not including) time `end`. Decide whether one person could attend every meeting, i.e. whether no two meetings overlap. Return `true` if they can, `false` otherwise.

**Example 1**
Input: intervals = [[0,30],[5,10],[15,20]]
Output: false
Why: [5,10] happens during [0,30].

**Example 2**
Input: intervals = [[7,10],[2,4]]
Output: true

**Constraints**
- 0 ≤ intervals.length ≤ 10⁴
- 0 ≤ start < end ≤ 10⁶

**Notes**: a meeting that ends at time t and another that starts at time t do **not** overlap. The intervals are not given in any particular order. An empty list means there is nothing to clash, so the answer is `true`.
