import java.util.*;

class Solution {
    public int numIslands(char[][] grid) {
        int m = grid.length, n = grid[0].length, count = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != '1') continue;
                count++;
                grid[i][j] = '0';
                q.add(new int[] {i, j});
                while (!q.isEmpty()) {
                    int[] c = q.poll();
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == '1') {
                            grid[r][k] = '0';
                            q.add(new int[] {r, k});
                        }
                    }
                }
            }
        }
        return count;
    }
}
