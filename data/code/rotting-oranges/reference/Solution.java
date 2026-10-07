import java.util.*;

class Solution {
    public int orangesRotting(int[][] grid) {
        int m = grid.length, n = grid[0].length, fresh = 0;
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 2) q.add(new int[] {i, j});
                else if (grid[i][j] == 1) fresh++;
            }
        int minutes = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (fresh > 0 && !q.isEmpty()) {
            minutes++;
            for (int s = q.size(); s > 0; s--) {
                int[] c = q.poll();
                for (int d = 0; d < 4; d++) {
                    int r = c[0] + dr[d], k = c[1] + dc[d];
                    if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1) {
                        grid[r][k] = 2;
                        fresh--;
                        q.add(new int[] {r, k});
                    }
                }
            }
        }
        return fresh == 0 ? minutes : -1;
    }
}
