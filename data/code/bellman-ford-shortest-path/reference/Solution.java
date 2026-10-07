import java.util.*;

class Solution {
    public int[] shortestPaths(int n, int[][] edges, int src) {
        final long INF = Long.MAX_VALUE / 4;
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[src] = 0;
        // n - 1 rounds of relaxing every edge (stop early once nothing changes)
        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (int[] e : edges) {
                if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) {
                    dist[e[1]] = dist[e[0]] + e[2];
                    changed = true;
                }
            }
            if (!changed) break;
        }
        // one more round: any improvement means a reachable negative cycle
        for (int[] e : edges) {
            if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) return new int[0];
        }
        int[] out = new int[n];
        for (int v = 0; v < n; v++) out[v] = dist[v] == INF ? Integer.MAX_VALUE : (int) dist[v];
        return out;
    }
}
