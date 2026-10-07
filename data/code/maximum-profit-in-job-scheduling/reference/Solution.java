import java.util.*;

class Solution {
    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {
        int n = startTime.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Integer.compare(endTime[a], endTime[b]));
        int[] ends = new int[n];
        for (int i = 0; i < n; i++) ends[i] = endTime[idx[i]];
        int[] dp = new int[n + 1]; // dp[i]: best using the first i jobs by end time
        for (int i = 1; i <= n; i++) {
            int j = idx[i - 1];
            // number of jobs (by end order) whose end <= startTime[j]
            int lo = 0, hi = i - 1;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (ends[mid] <= startTime[j]) lo = mid + 1; else hi = mid;
            }
            dp[i] = Math.max(dp[i - 1], dp[lo] + profit[j]);
        }
        return dp[n];
    }
}
