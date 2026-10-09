**Short answer:** Run Bellman–Ford for exactly `k + 1` rounds. After round `r`, `cost[v]` is the cheapest price to reach `v` using at most `r` flights. The key detail is to relax each round from a copy of the previous round's array, so one round cannot chain two flights. Return `cost[dst]`, or -1 if it is unreachable. O((k + 1) · E) time.

## Approach

**Brute force.** DFS over every route of up to `k + 1` flights. Exponential in the worst case.

**Why plain Dijkstra fails.** Dijkstra settles each city at its cheapest price, but the cheapest way into an intermediate city may use too many stops, while a pricier route with fewer stops is the one that reaches `dst` in time. In example 1, 0 → 1 → 2 → 3 costs 400 but needs 2 stops. The state must include the number of flights used.

**Key insight.** In Bellman–Ford, round `r` means "paths with at most `r` edges", but only if each round reads the previous round's values. If you read and write the same array, one round can relax 0 → 1 and then 1 → 2, using two flights in one round and breaking the limit.

**Optimal for these limits.** `k + 1` rounds over all flights, with a cloned array per round.

## Solution

```java
import java.util.*;

class Solution {
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        final int INF = Integer.MAX_VALUE / 2;
        int[] cost = new int[n];
        Arrays.fill(cost, INF);
        cost[src] = 0;
        // Round r allows routes of at most r flights; relax from the previous round's copy only.
        for (int round = 0; round <= k; round++) {
            int[] next = cost.clone();
            for (int[] f : flights) {
                if (cost[f[0]] < INF && cost[f[0]] + f[2] < next[f[1]]) next[f[1]] = cost[f[0]] + f[2];
            }
            cost = next;
        }
        return cost[dst] >= INF ? -1 : cost[dst];
    }
}
```

## Complexity

- **Time:** O((k + 1) · (E + n)). Each round scans every flight and copies the array. With n ≤ 100 and E ≤ 4950 that is under 5·10⁵ steps.
- **Space:** O(n) for the two cost arrays.

## Edge cases

- `k = 0`: only direct flights count (example 3).
- `dst` not reachable within the limit: return -1.
- `INF = MAX_VALUE / 2` and the `cost[f[0]] < INF` check prevent overflow when adding a price.
- Cycles in the flight graph are harmless: prices are positive and the number of rounds is fixed.

## Variations

- **BFS by number of flights:** expand level by level for up to `k + 1` levels, keeping the best price per city. Same idea, same cost.
- **Dijkstra on state (city, stops used):** a priority queue keyed by price, skipping states with more than `k` stops. It works if you do not drop a city just because it was reached more cheaply before; track the fewest stops seen per city instead.

Practise it in the app: Run / Submit on this page.
