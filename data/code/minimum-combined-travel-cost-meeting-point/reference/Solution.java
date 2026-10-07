import java.util.*;

class Solution {
    public long minTotalCost(int n, int[][] edges, int[] friends) {
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(new int[] {e[1], e[2]});
            adj.get(e[1]).add(new int[] {e[0], e[2]});
        }
        long[] total = new long[n];
        boolean[] reachable = new boolean[n];
        Arrays.fill(reachable, true);
        for (int f : friends) {
            long[] d = dijkstra(adj, n, f);
            for (int x = 0; x < n; x++) {
                if (d[x] == Long.MAX_VALUE) reachable[x] = false;
                else total[x] += d[x];
            }
        }
        long best = Long.MAX_VALUE;
        for (int x = 0; x < n; x++) if (reachable[x]) best = Math.min(best, total[x]);
        return best == Long.MAX_VALUE ? -1 : best;
    }

    private static long[] dijkstra(List<List<int[]>> adj, int n, int src) {
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[] {0, src});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (top[0] > dist[u]) continue;
            for (int[] e : adj.get(u)) {
                long nd = top[0] + e[1];
                if (nd < dist[e[0]]) {
                    dist[e[0]] = nd;
                    pq.add(new long[] {nd, e[0]});
                }
            }
        }
        return dist;
    }
}
