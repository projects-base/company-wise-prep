import java.util.*;

class Solution {
    public int smallestRangeII(int[] nums, int k) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length;
        int best = a[n - 1] - a[0]; // everyone moves the same way
        // In sorted order, the first i+1 go up and the rest go down.
        for (int i = 0; i + 1 < n; i++) {
            int hi = Math.max(a[i] + k, a[n - 1] - k);
            int lo = Math.min(a[0] + k, a[i + 1] - k);
            best = Math.min(best, hi - lo);
        }
        return best;
    }
}
