An undirected graph has `n` nodes numbered `0` to `n − 1` and is given as an adjacency list: `graph[u]` lists every node `v` joined to `u` by an edge. Return `true` if the nodes can be split into two groups so that every edge connects a node in one group to a node in the other (the graph is *bipartite*), and `false` otherwise. The graph may be disconnected — every component has to satisfy the rule.

**Example 1**
Input: graph = [[1,2,3],[0,2],[0,1,3],[0,2]]
Output: false
Why: nodes 0, 1 and 2 form a triangle, and an odd cycle can never be split in two.

**Example 2**
Input: graph = [[1,3],[0,2],[1,3],[0,2]]
Output: true
Why: groups {0, 2} and {1, 3}.

**Constraints**
- 1 ≤ n ≤ 10⁴ (the hidden tests use 4,000 nodes and 8,000 edges)
- no self-loops and no repeated edges; if `v` is in `graph[u]` then `u` is in `graph[v]`
- nodes with no edges have an empty list `[]`

**Notes**: an O(n + e) BFS/DFS colouring or union-find is expected.
