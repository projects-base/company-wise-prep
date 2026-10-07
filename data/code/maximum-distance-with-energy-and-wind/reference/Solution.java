import java.util.*;

class Solution {
    public long maxDistance(int[] a, int k) {
        final long NEG = Long.MIN_VALUE / 4;
        long[] dp = new long[k + 1]; // dp[e]: best distance so far ending the day with energy e
        Arrays.fill(dp, NEG);
        dp[k] = 0;
        long[] next = new long[k + 1];
        for (int x : a) {
            Arrays.fill(next, NEG);
            for (int e = 0; e <= k; e++) {
                if (dp[e] == NEG) continue;
                int rest = Math.min(k, e + 1);
                next[rest] = Math.max(next[rest], dp[e]);
                if (e >= 1) next[e - 1] = Math.max(next[e - 1], dp[e] + x);
            }
            long[] t = dp;
            dp = next;
            next = t;
        }
        long best = NEG;
        for (long v : dp) best = Math.max(best, v);
        return best;
    }
}
