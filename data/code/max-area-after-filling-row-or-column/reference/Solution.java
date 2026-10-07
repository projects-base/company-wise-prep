import java.util.*;

class Solution {
    public int largestComponentAfterFill(int[][] grid) {
        int R = grid.length, C = grid[0].length;
        int[][] id = new int[R][C];
        for (int[] row : id) Arrays.fill(row, -1);
        List<Integer> size = new ArrayList<>();
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        int best = 0;
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                if (grid[i][j] != 1 || id[i][j] >= 0) continue;
                int comp = size.size(), cnt = 0;
                id[i][j] = comp;
                stack.push(new int[] {i, j});
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    cnt++;
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < R && k >= 0 && k < C && grid[r][k] == 1 && id[r][k] < 0) {
                            id[r][k] = comp;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                size.add(cnt);
                best = Math.max(best, cnt); // a component away from the filled line survives unchanged
            }
        }
        int[] stamp = new int[size.size()];
        int token = 0;
        // Filling row r: the new group = C cells of the row + every component touching rows r-1..r+1,
        // minus the row's original 1s (already counted inside their components).
        for (int r = 0; r < R; r++) {
            token++;
            int total = C;
            for (int rr = r - 1; rr <= r + 1; rr++) {
                if (rr < 0 || rr >= R) continue;
                for (int k = 0; k < C; k++) {
                    int c = id[rr][k];
                    if (c < 0) continue;
                    if (rr == r) total--;
                    if (stamp[c] != token) {
                        stamp[c] = token;
                        total += size.get(c);
                    }
                }
            }
            best = Math.max(best, total);
        }
        for (int k = 0; k < C; k++) {
            token++;
            int total = R;
            for (int kk = k - 1; kk <= k + 1; kk++) {
                if (kk < 0 || kk >= C) continue;
                for (int r = 0; r < R; r++) {
                    int c = id[r][kk];
                    if (c < 0) continue;
                    if (kk == k) total--;
                    if (stamp[c] != token) {
                        stamp[c] = token;
                        total += size.get(c);
                    }
                }
            }
            best = Math.max(best, total);
        }
        return best;
    }
}
