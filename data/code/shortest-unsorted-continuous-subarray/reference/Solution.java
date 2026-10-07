import java.util.*;

class Solution {
    public int findUnsortedSubarray(int[] nums) {
        int n = nums.length;
        // end: last index smaller than the running max from the left.
        // start: first index larger than the running min from the right.
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, start = 0, end = -1;
        for (int i = 0; i < n; i++) {
            if (nums[i] < max) end = i; else max = nums[i];
            int j = n - 1 - i;
            if (nums[j] > min) start = j; else min = nums[j];
        }
        return end - start + 1;
    }
}
