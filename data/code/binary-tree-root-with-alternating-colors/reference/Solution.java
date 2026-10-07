import java.util.*;

class Solution {
    public List<Integer> findRoots(int n, int[][] edges, String colors, String pattern) {
        int len = pattern.length();
        int[] idx = new int[n];
        for (int v = 0; v < n; v++) {
            idx[v] = pattern.indexOf(colors.charAt(v));
            if (idx[v] < 0) return new ArrayList<>(); // colour never allowed at any depth
        }
        List<Integer>[] adj = adjacency(n, edges);

        // bad[r] = number of edges that break the pattern when the tree hangs from r.
        // Compute it for root 0, then re-root: moving the root across one edge flips only that edge.
        int[] parent = new int[n];
        int[] order = new int[n];
        Arrays.fill(parent, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        seen[0] = true;
        int bad0 = 0;
        while (head < tail) {
            int u = order[head++];
            for (int v : adj[u]) {
                if (seen[v]) continue;
                seen[v] = true;
                parent[v] = u;
                if (!down(idx, u, v, len)) bad0++;
                order[tail++] = v;
            }
        }
        int[] bad = new int[n];
        bad[0] = bad0;
        for (int i = 1; i < n; i++) { // BFS order: parent is done before child
            int v = order[i], u = parent[v];
            bad[v] = bad[u] - (down(idx, u, v, len) ? 0 : 1) + (down(idx, v, u, len) ? 0 : 1);
        }
        List<Integer> out = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            if (bad[r] == 0 && idx[r] == 0 && adj[r].size() <= 2) out.add(r);
        }
        return out;
    }

    /** true if `child` may sit directly below `par` in the pattern. */
    private boolean down(int[] idx, int par, int child, int len) {
        return idx[child] == (idx[par] + 1) % len;
    }

    @SuppressWarnings("unchecked")
    private List<Integer>[] adjacency(int n, int[][] edges) {
        List<Integer>[] adj = new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        for (int[] e : edges) {
            adj[e[0]].add(e[1]);
            adj[e[1]].add(e[0]);
        }
        return adj;
    }
}
