import java.util.*;

class Solution {
    private static final int[] DR = {1, -1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public int countLakes(int[][] grid, int row, int col) {
        int R = grid.length, C = grid[0].length;
        // 0 = unvisited, 1 = chosen island, 2 = reachable from the ocean, 3 = inside a lake
        int[][] mark = new int[R][C];

        // 1. Flood the chosen island over land cells.
        ArrayDeque<int[]> q = new ArrayDeque<>();
        mark[row][col] = 1;
        q.add(new int[] {row, col});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < R && c >= 0 && c < C && mark[r][c] == 0 && grid[r][c] == 1) {
                    mark[r][c] = 1;
                    q.add(new int[] {r, c});
                }
            }
        }

        // 2. Everything not on the island that touches the border can reach the ocean.
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if ((r == 0 || c == 0 || r == R - 1 || c == C - 1) && mark[r][c] == 0) flood(mark, r, c, 2);
            }
        }

        // 3. Each remaining non-island region is one lake.
        int lakes = 0;
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (mark[r][c] == 0) {
                    lakes++;
                    flood(mark, r, c, 3);
                }
            }
        }
        return lakes;
    }

    // Marks every unvisited cell reachable from (sr, sc) through unvisited cells.
    private static void flood(int[][] mark, int sr, int sc, int label) {
        int R = mark.length, C = mark[0].length;
        ArrayDeque<int[]> q = new ArrayDeque<>();
        mark[sr][sc] = label;
        q.add(new int[] {sr, sc});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < R && c >= 0 && c < C && mark[r][c] == 0) {
                    mark[r][c] = label;
                    q.add(new int[] {r, c});
                }
            }
        }
    }
}
