import java.util.*;

class Solution {
    public int[] shortestPath(int n, int[][] edges, int alice, int dest) {
        int[][] g = graph(n, edges);
        int[] toDest = bfs(g, dest);
        if (toDest[alice] < 0) return new int[0];
        // Walk forward, always taking the smallest neighbour that is one step closer to dest.
        int[] path = new int[toDest[alice] + 1];
        int cur = alice;
        for (int i = 0; i < path.length; i++) {
            path[i] = cur;
            int next = Integer.MAX_VALUE;
            for (int w : g[cur]) if (toDest[w] == toDest[cur] - 1 && w < next) next = w;
            cur = next;
        }
        return path;
    }

    public int[] nodesOnShortestPaths(int n, int[][] edges, int alice, int dest) {
        int[][] g = graph(n, edges);
        int[] da = bfs(g, alice), dd = bfs(g, dest);
        if (da[dest] < 0) return new int[0];
        int cnt = 0;
        for (int v = 0; v < n; v++) if (da[v] >= 0 && da[v] + dd[v] == da[dest]) cnt++;
        int[] out = new int[cnt];
        cnt = 0;
        for (int v = 0; v < n; v++) if (da[v] >= 0 && da[v] + dd[v] == da[dest]) out[cnt++] = v;
        return out;
    }

    public boolean canEscape(int n, int[][] edges, int alice, int bob, int dest) {
        int[][] g = graph(n, edges);
        int[] da = bfs(g, alice), dd = bfs(g, dest), db = bfs(g, bob);
        int total = da[dest];
        if (total < 0) return false;
        // good[v]: v is on a shortest path, safe, and a safe shortest route continues from v to dest.
        // Process nodes from dest backwards (decreasing distance from alice).
        List<List<Integer>> layers = new ArrayList<>();
        for (int i = 0; i <= total; i++) layers.add(new ArrayList<>());
        for (int v = 0; v < n; v++) if (da[v] >= 0 && da[v] + dd[v] == total) layers.get(da[v]).add(v);
        boolean[] good = new boolean[n];
        for (int d = total; d >= 0; d--) {
            for (int v : layers.get(d)) {
                boolean safe = db[v] < 0 || db[v] > da[v];
                if (!safe) continue;
                if (v == dest) { good[v] = true; continue; }
                for (int w : g[v]) {
                    if (good[w] && da[w] == da[v] + 1) { good[v] = true; break; }
                }
            }
        }
        return good[alice];
    }

    private int[][] graph(int n, int[][] edges) {
        int[] deg = new int[n];
        for (int[] e : edges) { deg[e[0]]++; deg[e[1]]++; }
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) g[i] = new int[deg[i]];
        for (int[] e : edges) {
            g[e[0]][--deg[e[0]]] = e[1];
            g[e[1]][--deg[e[1]]] = e[0];
        }
        return g;
    }

    private int[] bfs(int[][] g, int s) {
        int[] d = new int[g.length];
        Arrays.fill(d, -1);
        int[] q = new int[g.length];
        int h = 0, t = 0;
        q[t++] = s;
        d[s] = 0;
        while (h < t) {
            int u = q[h++];
            for (int w : g[u]) if (d[w] < 0) { d[w] = d[u] + 1; q[t++] = w; }
        }
        return d;
    }
}
