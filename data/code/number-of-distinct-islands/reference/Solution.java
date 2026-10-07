import java.util.*;

class Solution {
    public int numDistinctIslands(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        boolean[][] seen = new boolean[m][n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        Set<String> shapes = new HashSet<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != 1 || seen[i][j]) continue;
                // collect the island's cells as offsets from its first (row-major) cell
                List<Long> cells = new ArrayList<>();
                ArrayDeque<int[]> stack = new ArrayDeque<>();
                stack.push(new int[] {i, j});
                seen[i][j] = true;
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    cells.add((long) (c[0] - i) * 1000 + (c[1] - j));
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1 && !seen[r][k]) {
                            seen[r][k] = true;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                Collections.sort(cells);
                shapes.add(cells.toString());
            }
        }
        return shapes.size();
    }
}
