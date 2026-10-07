import java.util.*;

class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] out = new int[n - k + 1];
        int[] dq = new int[n]; // indices, values decreasing from head to tail
        int head = 0, tail = 0;
        for (int i = 0; i < n; i++) {
            if (head < tail && dq[head] <= i - k) head++;
            while (head < tail && nums[dq[tail - 1]] <= nums[i]) tail--;
            dq[tail++] = i;
            if (i >= k - 1) out[i - k + 1] = nums[dq[head]];
        }
        return out;
    }
}
