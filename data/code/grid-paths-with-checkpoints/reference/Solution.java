import java.util.*;

class Solution {
    private static final int MOD = 1_000_000_007;

    // Every move advances exactly one column, so a path visits exactly one cell per column.
    // A checkpoint therefore just forces the row used in its column (two different rows in the
    // same column make the answer 0). Sweep column by column keeping one row of counts.
    public int countPaths(int n, int m, int[][] checkpoints) {
        int[] forced = new int[m];
        Arrays.fill(forced, -1);
        for (int[] cp : checkpoints) {
            if (forced[cp[1]] != -1 && forced[cp[1]] != cp[0]) return 0;
            forced[cp[1]] = cp[0];
        }
        long[] dp = new long[n];
        dp[n - 1] = 1;
        applyForced(dp, forced[0]);
        for (int c = 1; c < m; c++) {
            long[] next = new long[n];
            for (int r = 0; r < n; r++) {
                long ways = dp[r];
                if (r > 0) ways += dp[r - 1];
                if (r + 1 < n) ways += dp[r + 1];
                next[r] = ways % MOD;
            }
            dp = next;
            applyForced(dp, forced[c]);
        }
        return (int) dp[n - 1];
    }

    private static void applyForced(long[] dp, int row) {
        if (row < 0) return;
        for (int r = 0; r < dp.length; r++) if (r != row) dp[r] = 0;
    }
}
