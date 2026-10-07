You are given an undirected graph with `n` vertices numbered `0` to `n − 1`. Each entry `edges[i] = [u, v, w]` is a road between `u` and `v` that costs `w` to travel in either direction. Return the minimum total cost of travelling from `source` to `target`, or `-1` if `target` cannot be reached.

**Example 1**
Input: n = 5, edges = [[0,1,4],[0,2,1],[2,1,2],[1,3,1],[2,3,5],[3,4,3]], source = 0, target = 4
Output: 7
Why: 0 → 2 → 1 → 3 → 4 costs 1 + 2 + 1 + 3 = 7.

**Example 2**
Input: n = 4, edges = [[0,1,2],[2,3,1]], source = 0, target = 3
Output: -1
Why: vertex 3 is in a different component from vertex 0.

**Example 3**
Input: n = 3, edges = [[0,1,10],[0,1,3],[1,2,3]], source = 2, target = 2
Output: 0
Why: you are already at the target.

**Constraints**
- 1 ≤ n ≤ 10⁵
- 0 ≤ edges.length ≤ 10⁵
- 0 ≤ u, v < n; there may be several edges between the same pair, and self-loops
- 0 ≤ w ≤ 10⁶
- 0 ≤ source, target < n

**Notes**: the answer can exceed the range of `int`, so it is returned as a `long`. The hidden tests include a graph with tens of thousands of vertices, so an O(n²) Dijkstra will be slow — use a priority queue.
