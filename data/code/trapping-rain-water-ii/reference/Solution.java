import java.util.*;

class Solution {
    public int trapRainWater(int[][] heightMap) {
        int m = heightMap.length, n = heightMap[0].length;
        if (m < 3 || n < 3) return 0;
        boolean[][] seen = new boolean[m][n];
        // min-heap of {level, row, col}; start from the whole border
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i == 0 || j == 0 || i == m - 1 || j == n - 1) {
                    seen[i][j] = true;
                    pq.add(new int[] {heightMap[i][j], i, j});
                }
            }
        }
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        int water = 0;
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            for (int d = 0; d < 4; d++) {
                int r = cur[1] + dr[d], c = cur[2] + dc[d];
                if (r < 0 || c < 0 || r >= m || c >= n || seen[r][c]) continue;
                seen[r][c] = true;
                water += Math.max(0, cur[0] - heightMap[r][c]);
                pq.add(new int[] {Math.max(cur[0], heightMap[r][c]), r, c});
            }
        }
        return water;
    }
}
