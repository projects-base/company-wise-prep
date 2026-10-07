import java.util.*;

class Solution {
    private static final long MOD = 1_000_000_007L;

    // If the unique maximum is v, choose which die shows it (k ways); every other die shows
    // one of 1..v-1, giving (v-1)^(k-1) outcomes. Sum over v.
    public int countOutcomes(int n, int k) {
        long total = 0;
        for (long v = 2; v <= n; v++) {
            total = (total + pow(v - 1, k - 1)) % MOD;
        }
        return (int) (total * (k % MOD) % MOD);
    }

    private static long pow(long b, long e) {
        long r = 1;
        b %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) r = r * b % MOD;
            b = b * b % MOD;
            e >>= 1;
        }
        return r;
    }
}
