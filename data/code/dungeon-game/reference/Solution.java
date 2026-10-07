import java.util.*;

class Solution {
    // need[i][j] = minimum health on entering room (i, j) to finish alive. Fill from the bottom-right.
    public int calculateMinimumHP(int[][] dungeon) {
        int m = dungeon.length, n = dungeon[0].length;
        int[] need = new int[n + 1];
        Arrays.fill(need, Integer.MAX_VALUE);
        for (int i = m - 1; i >= 0; i--) {
            int[] row = new int[n + 1];
            row[n] = Integer.MAX_VALUE;
            for (int j = n - 1; j >= 0; j--) {
                int after = (i == m - 1 && j == n - 1) ? 1 : Math.min(need[j], row[j + 1]);
                row[j] = Math.max(1, after - dungeon[i][j]);
            }
            need = row;
        }
        return need[0];
    }
}
