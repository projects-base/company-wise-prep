import java.util.*;

class Solution {
    public boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n]; // 0 = unseen, 1 / -1 = the two groups
        int[] queue = new int[n];
        for (int start = 0; start < n; start++) {
            if (color[start] != 0) continue;
            color[start] = 1;
            int head = 0, tail = 0;
            queue[tail++] = start;
            while (head < tail) {
                int u = queue[head++];
                for (int v : graph[u]) {
                    if (color[v] == 0) {
                        color[v] = -color[u];
                        queue[tail++] = v;
                    } else if (color[v] == color[u]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
