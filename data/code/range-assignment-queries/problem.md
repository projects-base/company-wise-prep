You are given an array `arr` and a list of queries `queries[i] = [l, r, k]` (0-based, `l ≤ r`, both ends inclusive). Apply the queries in order. Query `[l, r, k]` sets every element `arr[l], arr[l+1], …, arr[r]` to `k`, overwriting whatever was there. Return the array after all queries.

**Example 1**
Input: arr = [1,2,3,4,5], queries = [[0,2,9],[1,3,7]]
Output: [9,7,7,7,5]
Why: the first query gives [9,9,9,4,5]. The second overwrites indices 1..3 with 7.

**Example 2**
Input: arr = [0,0,0], queries = []
Output: [0,0,0]

**Example 3**
Input: arr = [5,5,5,5], queries = [[0,3,1],[2,2,8],[0,1,1]]
Output: [1,1,8,1]

**Constraints**
- 1 ≤ arr.length ≤ 10⁵
- 0 ≤ queries.length ≤ 10⁵
- 0 ≤ l ≤ r < arr.length; arr[i] and k fit in an `int`

**Notes**: writing every cell of every query is O(n · q) in the worst case. Two efficient approaches: a segment tree with lazy assignment, or processing the queries **backwards** so that each cell is written only once, by the last query that covers it, skipping cells already written with a "next unwritten index" union-find. Follow-up: if every query used the same `k`, the problem reduces to merging intervals.
