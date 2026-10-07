import java.util.*;

class Solution {
    public int countStrings(int k) {
        final long MOD = 1_000_000_007L;
        // dp[endsWithA][cUsed] = number of valid strings of the current length
        long[][] dp = new long[2][2];
        dp[0][0] = 1; // the empty string: does not end with 'a', no 'c' used
        for (int len = 0; len < k; len++) {
            long[][] nx = new long[2][2];
            for (int endA = 0; endA < 2; endA++) {
                for (int c = 0; c < 2; c++) {
                    long cur = dp[endA][c];
                    if (cur == 0) continue;
                    nx[0][c] = (nx[0][c] + cur) % MOD;                // append 'b'
                    if (endA == 0) nx[1][c] = (nx[1][c] + cur) % MOD; // append 'a'
                    if (c == 0) nx[0][1] = (nx[0][1] + cur) % MOD;    // append the single 'c'
                }
            }
            dp = nx;
        }
        return (int) ((dp[0][0] + dp[0][1] + dp[1][0] + dp[1][1]) % MOD);
    }
}
