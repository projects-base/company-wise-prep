import java.util.*;

class Solution {
    public int swimInWater(int[][] grid) {
        int n = grid.length;
        // Dijkstra where a path's cost is the highest elevation on it.
        int[][] best = new int[n][n];
        for (int[] row : best) Arrays.fill(row, Integer.MAX_VALUE);
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        best[0][0] = grid[0][0];
        pq.add(new int[] {grid[0][0], 0, 0});
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int t = cur[0], r = cur[1], c = cur[2];
            if (t > best[r][c]) continue;
            if (r == n - 1 && c == n - 1) return t;
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr < 0 || nr >= n || nc < 0 || nc >= n) continue;
                int nt = Math.max(t, grid[nr][nc]);
                if (nt < best[nr][nc]) {
                    best[nr][nc] = nt;
                    pq.add(new int[] {nt, nr, nc});
                }
            }
        }
        return -1;
    }
}
