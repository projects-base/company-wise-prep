import java.util.*;

class Solution {
    public int shortestBridge(int[][] grid) {
        int n = grid.length;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>();
        // Mark the first island (iterative flood fill) and use all its cells as BFS sources.
        outer:
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    ArrayDeque<int[]> stack = new ArrayDeque<>();
                    stack.push(new int[] {i, j});
                    dist[i][j] = 0;
                    while (!stack.isEmpty()) {
                        int[] c = stack.pop();
                        q.add(c);
                        for (int d = 0; d < 4; d++) {
                            int r = c[0] + dr[d], k = c[1] + dc[d];
                            if (r >= 0 && r < n && k >= 0 && k < n && grid[r][k] == 1 && dist[r][k] == -1) {
                                dist[r][k] = 0;
                                stack.push(new int[] {r, k});
                            }
                        }
                    }
                    break outer;
                }
            }
        }
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + dr[d], k = c[1] + dc[d];
                if (r < 0 || r >= n || k < 0 || k >= n || dist[r][k] != -1) continue;
                if (grid[r][k] == 1) return dist[c[0]][c[1]];
                dist[r][k] = dist[c[0]][c[1]] + 1;
                q.add(new int[] {r, k});
            }
        }
        return -1;
    }
}
