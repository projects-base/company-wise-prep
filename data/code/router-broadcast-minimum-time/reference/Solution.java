import java.util.*;

class Solution {
    public int minTimeToTurnOn(int[][] routers, int radius, int source) {
        int n = routers.length;
        long r2 = (long) radius * radius;
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        dist[source] = 0;
        int[] queue = new int[n];
        int head = 0, tail = 0, last = 0;
        queue[tail++] = source;
        while (head < tail) {
            int u = queue[head++];
            last = dist[u];
            for (int v = 0; v < n; v++) {
                if (dist[v] != -1) continue;
                long dx = routers[u][0] - routers[v][0], dy = routers[u][1] - routers[v][1];
                if (dx * dx + dy * dy <= r2) {
                    dist[v] = dist[u] + 1;
                    queue[tail++] = v;
                }
            }
        }
        return tail == n ? last : -1;
    }
}
