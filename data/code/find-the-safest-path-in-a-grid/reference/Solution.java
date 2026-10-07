import java.util.*;

class Solution {
    private static final int[] DR = {1, -1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public int maximumSafenessFactor(List<List<Integer>> grid) {
        int n = grid.size();
        // 1. Multi-source BFS from every thief gives each cell's distance to the nearest thief.
        int[][] dist = new int[n][n];
        for (int[] d : dist) Arrays.fill(d, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (grid.get(r).get(c) == 1) {
                    dist[r][c] = 0;
                    q.add(new int[] {r, c});
                }
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < n && c >= 0 && c < n && dist[r][c] == -1) {
                    dist[r][c] = dist[p[0]][p[1]] + 1;
                    q.add(new int[] {r, c});
                }
            }
        }
        // 2. Widest-path search: always extend from the frontier cell with the largest safeness so far.
        int[][] best = new int[n][n];
        for (int[] b : best) Arrays.fill(b, -1);
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(b[0], a[0]));
        best[0][0] = dist[0][0];
        pq.add(new int[] {dist[0][0], 0, 0});
        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int s = top[0], r0 = top[1], c0 = top[2];
            if (s < best[r0][c0]) continue;
            if (r0 == n - 1 && c0 == n - 1) return s;
            for (int d = 0; d < 4; d++) {
                int r = r0 + DR[d], c = c0 + DC[d];
                if (r < 0 || r >= n || c < 0 || c >= n) continue;
                int ns = Math.min(s, dist[r][c]);
                if (ns > best[r][c]) {
                    best[r][c] = ns;
                    pq.add(new int[] {ns, r, c});
                }
            }
        }
        return best[n - 1][n - 1];
    }
}
