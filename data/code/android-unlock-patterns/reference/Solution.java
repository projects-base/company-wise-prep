import java.util.*;

class Solution {
    public int numberOfPatterns(int m, int n) {
        // skip[a][b] = the dot exactly between a and b, or 0 if none
        int[][] skip = new int[10][10];
        skip[1][3] = skip[3][1] = 2;
        skip[4][6] = skip[6][4] = 5;
        skip[7][9] = skip[9][7] = 8;
        skip[1][7] = skip[7][1] = 4;
        skip[2][8] = skip[8][2] = 5;
        skip[3][9] = skip[9][3] = 6;
        skip[1][9] = skip[9][1] = 5;
        skip[3][7] = skip[7][3] = 5;
        boolean[] used = new boolean[10];
        int total = 0;
        // corners and edge-midpoints are symmetric
        total += 4 * dfs(1, 1, m, n, skip, used);
        total += 4 * dfs(2, 1, m, n, skip, used);
        total += dfs(5, 1, m, n, skip, used);
        return total;
    }

    private int dfs(int cur, int len, int m, int n, int[][] skip, boolean[] used) {
        int count = len >= m ? 1 : 0;
        if (len == n) return count;
        used[cur] = true;
        for (int next = 1; next <= 9; next++) {
            if (used[next]) continue;
            int mid = skip[cur][next];
            if (mid != 0 && !used[mid]) continue;
            count += dfs(next, len + 1, m, n, skip, used);
        }
        used[cur] = false;
        return count;
    }
}
