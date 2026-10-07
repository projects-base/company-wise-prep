import java.util.*;

class Solution {
    public int minDifficulty(int[] jobDifficulty, int d) {
        int n = jobDifficulty.length;
        if (n < d) return -1;
        final int INF = Integer.MAX_VALUE / 2;
        // dp[i] = best for the first i jobs using the current number of days.
        int[] dp = new int[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int day = 1; day <= d; day++) {
            int[] next = new int[n + 1];
            Arrays.fill(next, INF);
            for (int i = day; i <= n; i++) {
                int mx = 0;
                // the last day covers jobs j..i-1
                for (int j = i - 1; j >= day - 1; j--) {
                    mx = Math.max(mx, jobDifficulty[j]);
                    if (dp[j] < INF) next[i] = Math.min(next[i], dp[j] + mx);
                }
            }
            dp = next;
        }
        return dp[n];
    }
}
