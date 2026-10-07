import java.util.*;

class Solution {
    private static final int[] DR = {-1, 1, 0, 0}, DC = {0, 0, -1, 1};
    private static final char[] NAME = {'U', 'D', 'L', 'R'};

    public String escapeSequence(String[] grid) {
        int rows = grid.length, cols = grid[0].length();
        int exit = -1;
        List<Integer> open = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = grid[r].charAt(c);
                if (ch == 'E') exit = r * cols + c;
                if (ch != '#') open.add(r * cols + c);
            }
        }
        // Distance to the exit from every open cell (moves are reversible, so BFS from the exit).
        int[] dist = new int[rows * cols];
        Arrays.fill(dist, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        dist[exit] = 0;
        q.add(exit);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int d = 0; d < 4; d++) {
                int v = step(grid, u, d, cols);
                if (v != u && dist[v] < 0) { dist[v] = dist[u] + 1; q.add(v); }
            }
        }
        for (int cell : open) if (dist[cell] < 0) return "";

        // Greedy: track the set of positions a token could be in. Repeatedly take one that has
        // not escaped and walk it to the exit along a shortest path; apply the same moves to all.
        // Each round removes at least one position, so it ends after fewer than |open| rounds.
        StringBuilder out = new StringBuilder();
        Set<Integer> pos = new HashSet<>(open);
        pos.remove(exit);
        while (!pos.isEmpty()) {
            int cur = pos.iterator().next();
            while (cur != exit) {
                int dir = -1;
                for (int d = 0; d < 4; d++) {
                    int v = step(grid, cur, d, cols);
                    if (v != cur && dist[v] == dist[cur] - 1) { dir = d; break; }
                }
                out.append(NAME[dir]);
                Set<Integer> next = new HashSet<>();
                for (int p : pos) {
                    int v = step(grid, p, dir, cols);
                    if (v != exit) next.add(v);
                }
                pos = next;
                cur = step(grid, cur, dir, cols);
            }
        }
        return out.toString();
    }

    /** Cell reached from u moving in direction d (u itself if blocked). */
    private int step(String[] grid, int u, int d, int cols) {
        int r = u / cols + DR[d], c = u % cols + DC[d];
        if (r < 0 || r >= grid.length || c < 0 || c >= cols || grid[r].charAt(c) == '#') return u;
        return r * cols + c;
    }
}
