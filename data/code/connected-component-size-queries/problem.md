You are given an undirected graph with `n` nodes labelled `0` to `n - 1` and a list of `edges`, where `edges[i] = [u, v]` connects nodes `u` and `v`. The graph may be split into several disconnected pieces. You are also given an array `queries`. For each `queries[i]`, count how many **other** nodes can be reached from node `queries[i]` by following edges (a node never counts itself). Return the answers as an array in the same order as `queries`.

**Example 1**
Input: n = 6, edges = [[0,1],[1,2],[3,4]], queries = [0,3,5,2]
Output: [2,1,0,2]
Why: the pieces are {0,1,2}, {3,4} and {5}. Node 0 reaches 1 and 2; node 3 reaches 4; node 5 reaches nothing; node 2 reaches 0 and 1.

**Example 2**
Input: n = 3, edges = [], queries = [0,1,2]
Output: [0,0,0]

**Constraints**
- 1 ≤ n ≤ 10⁵
- 0 ≤ edges.length ≤ 10⁵; 0 ≤ u, v < n
- an edge may appear more than once, and `u == v` (a self-loop) is allowed — neither adds any new reachable node
- 1 ≤ queries.length ≤ 10⁵; 0 ≤ queries[i] < n, and the same node may be queried many times

**Notes**: the hidden tests include 8,000 queries on a graph where most nodes sit in one huge piece, so running a fresh DFS for every query (O(q · n)) will be slow. Compute each piece's size once — with DFS/BFS labelling or with a union-find (DSU) — and answer each query in O(1).
