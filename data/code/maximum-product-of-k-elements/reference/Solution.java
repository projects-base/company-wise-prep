import java.util.*;

class Solution {
    public long maxProduct(int[] nums, int k) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length;
        int l = 0, r = n - 1;
        long prod = 1;
        if (k % 2 == 1) {
            if (a[r] <= 0) {
                // every value is <= 0 and k is odd: the product cannot be positive,
                // so take the k values closest to zero (the k largest).
                for (int i = 0; i < k; i++) prod *= a[n - 1 - i];
                return prod;
            }
            prod = a[r--];
            k--;
        }
        // k is even now: take pairs, either the two smallest or the two largest remaining.
        while (k > 0) {
            long left = (long) a[l] * a[l + 1];
            long right = (long) a[r] * a[r - 1];
            if (left > right) {
                prod *= left;
                l += 2;
            } else {
                prod *= right;
                r -= 2;
            }
            k -= 2;
        }
        return prod;
    }
}
