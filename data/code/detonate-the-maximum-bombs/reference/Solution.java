import java.util.*;

class Solution {
    // Directed graph i -> j when j's centre is inside i's circle; BFS from every bomb.
    public int maximumDetonation(int[][] bombs) {
        int n = bombs.length;
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            long r = bombs[i][2];
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                long dx = bombs[i][0] - bombs[j][0], dy = bombs[i][1] - bombs[j][1];
                if (dx * dx + dy * dy <= r * r) adj.get(i).add(j);
            }
        }
        int best = 0;
        for (int s = 0; s < n; s++) {
            boolean[] seen = new boolean[n];
            ArrayDeque<Integer> q = new ArrayDeque<>();
            seen[s] = true;
            q.add(s);
            int count = 0;
            while (!q.isEmpty()) {
                int u = q.poll();
                count++;
                for (int v : adj.get(u)) {
                    if (!seen[v]) {
                        seen[v] = true;
                        q.add(v);
                    }
                }
            }
            best = Math.max(best, count);
        }
        return best;
    }
}
