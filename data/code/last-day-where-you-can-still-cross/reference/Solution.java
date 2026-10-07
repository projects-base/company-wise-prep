import java.util.*;

class Solution {
    private int[] parent;

    // Run time backwards: start with all water and turn cells back into land from the last day.
    // Two virtual nodes stand for "the top row" and "the bottom row". The first moment (going
    // backwards) they join, the field before that day's flood was crossable, so the answer is
    // that day minus one.
    public int latestDayToCross(int row, int col, int[][] cells) {
        int n = row * col, top = n, bottom = n + 1;
        parent = new int[n + 2];
        for (int i = 0; i < n + 2; i++) parent[i] = i;
        boolean[] land = new boolean[n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        for (int day = cells.length; day >= 1; day--) {
            int r = cells[day - 1][0] - 1, c = cells[day - 1][1] - 1, id = r * col + c;
            land[id] = true;
            if (r == 0) union(id, top);
            if (r == row - 1) union(id, bottom);
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr >= 0 && nr < row && nc >= 0 && nc < col && land[nr * col + nc]) union(id, nr * col + nc);
            }
            if (find(top) == find(bottom)) return day - 1;
        }
        return 0;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private void union(int a, int b) {
        parent[find(a)] = find(b);
    }
}
