import java.util.*;

class Solution {
    public int minimumFinishTime(int[][] tires, int changeTime, int numLaps) {
        final long INF = Long.MAX_VALUE / 4;
        // best[k]: fastest time for k consecutive laps on one fresh tyre (no change)
        long[] best = new long[numLaps + 1];
        Arrays.fill(best, INF);
        for (int[] t : tires) {
            long f = t[0], r = t[1];
            long lap = f, total = 0;
            for (int k = 1; k <= numLaps; k++) {
                total += lap;
                if (total < best[k]) best[k] = total;
                // once a lap costs more than changing to a fresh tyre's first lap, stop extending
                if (lap >= changeTime + f) break;
                lap *= r;
                if (lap > INF / 2) break;
            }
        }
        long[] dp = new long[numLaps + 1];
        dp[0] = 0;
        for (int i = 1; i <= numLaps; i++) {
            dp[i] = INF;
            for (int k = 1; k <= i; k++) {
                if (best[k] >= INF) break;
                long cand = best[k] + (k == i ? 0 : changeTime + dp[i - k]);
                dp[i] = Math.min(dp[i], cand);
            }
        }
        return (int) dp[numLaps];
    }
}
