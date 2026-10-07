import java.util.*;

class Solution {
    public long shortestPath(int n, int[][] edges, int source, int target) {
        // Adjacency lists in compact arrays.
        int m = edges.length;
        int[] head = new int[n], next = new int[2 * m], to = new int[2 * m], wt = new int[2 * m];
        Arrays.fill(head, -1);
        int k = 0;
        for (int[] e : edges) {
            to[k] = e[1]; wt[k] = e[2]; next[k] = head[e[0]]; head[e[0]] = k++;
            to[k] = e[0]; wt[k] = e[2]; next[k] = head[e[1]]; head[e[1]] = k++;
        }
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[source] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[] {0, source});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (top[0] > dist[u]) continue; // stale entry
            if (u == target) return top[0];
            for (int e = head[u]; e != -1; e = next[e]) {
                long nd = top[0] + wt[e];
                if (nd < dist[to[e]]) {
                    dist[to[e]] = nd;
                    pq.add(new long[] {nd, to[e]});
                }
            }
        }
        return -1;
    }
}
