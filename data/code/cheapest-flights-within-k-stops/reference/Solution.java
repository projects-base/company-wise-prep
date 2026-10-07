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
