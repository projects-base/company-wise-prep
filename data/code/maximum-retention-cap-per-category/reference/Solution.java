import java.util.*;

class Solution {
    public int maxRetentionCap(int[] counts, long maxEntries) {
        long total = 0;
        int max = 0;
        for (int c : counts) {
            total += c;
            max = Math.max(max, c);
        }
        if (total <= maxEntries) return -1;
        // retained(X) is non-decreasing; retained(0) = 0 <= maxEntries and retained(max) = total > maxEntries.
        int lo = 0, hi = max - 1; // answer lies in [0, max - 1]
        while (lo < hi) {
            int mid = lo + (hi - lo + 1) / 2;
            if (retained(counts, mid) <= maxEntries) lo = mid; else hi = mid - 1;
        }
        return lo;
    }

    private static long retained(int[] counts, int x) {
        long s = 0;
        for (int c : counts) s += Math.min(c, x);
        return s;
    }
}
