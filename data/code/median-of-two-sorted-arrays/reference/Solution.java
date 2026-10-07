import java.util.*;

class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
        int m = nums1.length, n = nums2.length, half = (m + n + 1) / 2;
        int lo = 0, hi = m;
        while (lo <= hi) {
            int i = (lo + hi) / 2; // elements taken from nums1 into the left half
            int j = half - i;      // elements taken from nums2
            int aLeft = i == 0 ? Integer.MIN_VALUE : nums1[i - 1];
            int aRight = i == m ? Integer.MAX_VALUE : nums1[i];
            int bLeft = j == 0 ? Integer.MIN_VALUE : nums2[j - 1];
            int bRight = j == n ? Integer.MAX_VALUE : nums2[j];
            if (aLeft > bRight) hi = i - 1;
            else if (bLeft > aRight) lo = i + 1;
            else {
                int leftMax = Math.max(aLeft, bLeft);
                if ((m + n) % 2 == 1) return leftMax;
                int rightMin = Math.min(aRight, bRight);
                return (leftMax + (double) rightMin) / 2.0;
            }
        }
        throw new IllegalStateException("arrays are not sorted");
    }
}
