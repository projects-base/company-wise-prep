import java.util.*;

class Solution {
    // A value is always found exactly when it is larger than everything to its left
    // and smaller than everything to its right.
    public int binarySearchableNumbers(int[] nums) {
        int n = nums.length;
        boolean[] okLeft = new boolean[n];
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            okLeft[i] = nums[i] > max;
            max = Math.max(max, nums[i]);
        }
        int min = Integer.MAX_VALUE, count = 0;
        for (int i = n - 1; i >= 0; i--) {
            if (okLeft[i] && nums[i] < min) count++;
            min = Math.min(min, nums[i]);
        }
        return count;
    }
}
