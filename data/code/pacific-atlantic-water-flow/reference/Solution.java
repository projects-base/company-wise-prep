import java.util.*;

class Solution {
    private static final int[] DR = {1, -1, 0, 0}, DC = {0, 0, 1, -1};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        boolean[][] pac = new boolean[m][n], atl = new boolean[m][n];
        ArrayDeque<int[]> pq = new ArrayDeque<>(), aq = new ArrayDeque<>();
        for (int r = 0; r < m; r++) {
            mark(pac, pq, r, 0);
            mark(atl, aq, r, n - 1);
        }
        for (int c = 0; c < n; c++) {
            mark(pac, pq, 0, c);
            mark(atl, aq, m - 1, c);
        }
        climb(heights, pac, pq);
        climb(heights, atl, aq);
        List<List<Integer>> out = new ArrayList<>();
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++)
                if (pac[r][c] && atl[r][c]) out.add(List.of(r, c));
        return out;
    }

    private static void mark(boolean[][] seen, ArrayDeque<int[]> q, int r, int c) {
        if (!seen[r][c]) {
            seen[r][c] = true;
            q.add(new int[] {r, c});
        }
    }

    // BFS uphill: from a cell that reaches the ocean, any neighbour at least as high also reaches it.
    private static void climb(int[][] h, boolean[][] seen, ArrayDeque<int[]> q) {
        int m = h.length, n = h[0].length;
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + DR[d], k = c[1] + DC[d];
                if (r >= 0 && r < m && k >= 0 && k < n && !seen[r][k] && h[r][k] >= h[c[0]][c[1]]) {
                    seen[r][k] = true;
                    q.add(new int[] {r, k});
                }
            }
        }
    }
}
