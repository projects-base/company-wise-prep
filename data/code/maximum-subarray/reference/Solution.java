import java.util.*;

class Solution {
    public int maxSubArray(int[] nums) {
        int best = nums[0], cur = nums[0];
        for (int i = 1; i < nums.length; i++) {
            cur = Math.max(nums[i], cur + nums[i]); // extend the block or start fresh here
            best = Math.max(best, cur);
        }
        return best;
    }
}
