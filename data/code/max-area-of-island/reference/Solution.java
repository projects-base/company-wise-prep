import java.util.*;

class Solution {
    public int maxAreaOfIsland(int[][] grid) {
        int m = grid.length, n = grid[0].length, best = 0;
        boolean[][] seen = new boolean[m][n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != 1 || seen[i][j]) continue;
                seen[i][j] = true;
                stack.push(new int[] {i, j});
                int area = 0;
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    area++;
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1 && !seen[r][k]) {
                            seen[r][k] = true;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                best = Math.max(best, area);
            }
        }
        return best;
    }
}
