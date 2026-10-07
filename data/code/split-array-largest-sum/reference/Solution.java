import java.util.*;

class Solution {
    public int splitArray(int[] nums, int k) {
        long lo = 0, hi = 0;
        for (int x : nums) { lo = Math.max(lo, x); hi += x; }
        // Smallest cap such that a greedy left-to-right cut needs at most k pieces.
        // At most k pieces is enough: pieces can always be split further (n >= k).
        while (lo < hi) {
            long mid = (lo + hi) >>> 1;
            if (pieces(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        return (int) lo;
    }

    private int pieces(int[] nums, long cap) {
        int count = 1;
        long cur = 0;
        for (int x : nums) {
            if (cur + x > cap) { count++; cur = 0; }
            cur += x;
        }
        return count;
    }
}
