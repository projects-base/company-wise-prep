import java.util.*;

class Solution {
    public long countSubarrays(int[] nums, int minK, int maxK) {
        long total = 0;
        int lastBad = -1, lastMin = -1, lastMax = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] < minK || nums[i] > maxK) lastBad = i;
            if (nums[i] == minK) lastMin = i;
            if (nums[i] == maxK) lastMax = i;
            // Subarrays ending at i are valid when they start after lastBad and at or before both last hits.
            total += Math.max(0, Math.min(lastMin, lastMax) - lastBad);
        }
        return total;
    }
}
