A directed graph has `n` nodes numbered `0` to `n-1`. `edges[i] = [u, v, w]` is an edge from `u` to `v` with weight `w`; weights may be **negative**. There can be several edges between the same pair of nodes, and self-loops are allowed.

Compute the shortest-path distance from the node `src` to every node and return them as an array `dist` of length `n`:

- `dist[src] = 0` (unless rule 3 applies),
- if node `v` cannot be reached from `src`, `dist[v] = 2147483647` (`Integer.MAX_VALUE`),
- if a **negative cycle** (a cycle whose weights sum to less than 0) can be reached from `src`, shortest paths are not well defined: return an empty array `[]` instead.

Negative cycles that cannot be reached from `src` do not matter.

**Example 1**
Input: n = 4, edges = [[0,1,4],[0,2,5],[1,2,-3],[2,3,4]], src = 0
Output: [0,4,1,5]
Why: going 0 → 1 → 2 costs 4 − 3 = 1, cheaper than the direct edge of 5.

**Example 2**
Input: n = 3, edges = [[0,1,1],[1,2,-2],[2,1,1]], src = 0
Output: []
Why: 1 → 2 → 1 has total weight −1 and is reachable from 0.

**Example 3**
Input: n = 3, edges = [[1,2,3]], src = 0
Output: [0,2147483647,2147483647]

**Constraints**
- 1 ≤ n ≤ 1000, 0 ≤ edges.length ≤ 5000
- 0 ≤ u, v, src < n
- −10⁴ ≤ w ≤ 10⁴

**Notes**: when there is no reachable negative cycle every shortest distance fits comfortably in an `int`. Bellman–Ford in O(n · edges) is the intended solution.
