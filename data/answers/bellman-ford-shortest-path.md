**Short answer:** Set `dist[src] = 0` and every other distance to infinity. Then relax every edge `n − 1` times. A shortest simple path has at most `n − 1` edges, so after that many rounds every distance is final. Do one more round: if any edge can still be relaxed, a negative cycle is reachable from `src`, so return `[]`. Only relax edges whose start node is already reachable. O(n · E) time.

## Approach

- **Why not Dijkstra:** Dijkstra treats a node as final once it leaves the priority queue. A negative edge found later can make that node cheaper, so the answer is wrong. Bellman–Ford assumes nothing about weights.
- **Key insight:** after round k, `dist[v]` is at most the cost of the best path using at most k edges. A shortest path that contains no cycle has at most `n − 1` edges. So `n − 1` rounds are enough, unless a negative cycle exists. In that case some distance keeps falling forever, and round `n` will still find an improvement.
- **Two details that matter:**
  1. Skip edges from unreachable nodes (`dist[u] == INF`). Otherwise `INF + negative weight` looks like an improvement and an unreachable negative cycle would wrongly be reported.
  2. Stop early when a round changes nothing. On many graphs this finishes after a few rounds.
- Use `long` for distances so that the infinity value plus a weight cannot overflow.

## Solution

```java
import java.util.*;

class Solution {
    public int[] shortestPaths(int n, int[][] edges, int src) {
        final long INF = Long.MAX_VALUE / 4;
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[src] = 0;
        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (int[] e : edges) {
                if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) {
                    dist[e[1]] = dist[e[0]] + e[2];
                    changed = true;
                }
            }
            if (!changed) break;                     // already stable
        }
        // One more round: any improvement means a reachable negative cycle.
        for (int[] e : edges) {
            if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) return new int[0];
        }
        int[] out = new int[n];
        for (int v = 0; v < n; v++) out[v] = dist[v] == INF ? Integer.MAX_VALUE : (int) dist[v];
        return out;
    }
}
```

The early break is safe for the cycle check: if a round changes nothing, the next round would not either, so the final check also finds nothing.

## Complexity

- **Time:** O(n · E) in the worst case — 1000 × 5000 = 5 × 10⁶ relaxations here.
- **Space:** O(n) for the distances. The edge list is used as given.

## Edge cases

- A negative cycle that `src` cannot reach → ignored, because its nodes stay at INF and are never relaxed.
- A negative self-loop on a reachable node (`[u, u, -1]`) → a negative cycle → `[]`.
- Parallel edges → each is relaxed, and the cheapest wins.
- No edges → `[0, MAX, MAX, ...]`.
- `n = 1` → zero rounds, then the check → `[0]`, or `[]` if there is a negative self-loop.

## Variations

- **SPFA:** a queue-based Bellman–Ford that only re-relaxes edges out of nodes whose distance changed. It is often faster in practice, but the worst case is the same.
- **At most K edges** (Cheapest Flights Within K Stops): run exactly K+1 rounds, and relax from a *copy* of the previous round's distances so that one round cannot use two edges.
- **Mark the nodes affected by a negative cycle** as −∞ instead of failing the whole query: after round n, BFS from every node that still improved.
- **All pairs:** Floyd–Warshall O(n³), or Johnson's algorithm (Bellman–Ford once to reweight, then Dijkstra from each node).

Practise it in the app: Run / Submit on this page.
