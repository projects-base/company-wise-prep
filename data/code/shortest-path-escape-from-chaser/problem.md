An undirected, unweighted graph has `n` nodes numbered `0..n-1` and the given `edges`. Alice starts at node `alice` and wants to reach node `dest`. Implement three methods:

1. `shortestPath(n, edges, alice, dest)`: return one shortest path from `alice` to `dest` as the list of nodes visited (both ends included). If there are several, return the **lexicographically smallest** one. Return an empty array if `dest` cannot be reached.
2. `nodesOnShortestPaths(n, edges, alice, dest)`: return every node that lies on **at least one** shortest path from `alice` to `dest`, in increasing order. Return an empty array if `dest` cannot be reached.
3. `canEscape(n, edges, alice, bob, dest)`: Bob, a chaser, starts at node `bob`. Every second Alice moves along one edge of a shortest path towards `dest`, and at the same time Bob either moves along one edge or stays put. Alice chooses her whole route in advance and Bob knows it. Bob catches her at node `v` if he can be standing on `v` when she arrives there, which is exactly when `distBob(v) ≤ distAlice(v)`. This applies to her start node (time 0) and to `dest` as well, and it also covers them crossing on an edge. Return `true` if some shortest path lets Alice reach `dest` without being caught. Return `false` if `dest` is unreachable.

The judge calls all three methods and prints `[shortestPath, nodesOnShortestPaths, canEscape]`.

**Example 1**
Input: n = 6, edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[2,5]], alice = 0, bob = 5, dest = 4
Output: [[0,1,3,4],[0,1,2,3,4],false]
Why: the shortest routes are 0-1-3-4 and 0-2-3-4, and the first is smaller. Bob is 3 steps from node 4, and Alice also needs 3 steps to get there, so he can wait for her at `dest`.

**Example 2**
Input: n = 7, edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[5,6],[6,2]], alice = 0, bob = 5, dest = 4
Output: [[0,1,3,4],[0,1,2,3,4],true]
Why: Bob now needs 3 seconds to reach node 1, 3 to reach node 3 and 4 to reach node 4. Alice is on those nodes at times 1, 2 and 3, always ahead of him.

**Example 3**
Input: n = 3, edges = [[0,1]], alice = 0, bob = 1, dest = 2
Output: [[],[],false]
Why: node 2 cannot be reached.

**Constraints**
- 1 ≤ n ≤ 10⁵, 0 ≤ edges.length ≤ 2·10⁵
- no self-loops or repeated edges
- 0 ≤ alice, bob, dest < n (they need not be different; if `alice == dest` the path is just `[alice]`)
