import java.util.*;

class Solution {
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> lastIndex = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer prev = lastIndex.put(nums[i], i);
            if (prev != null && i - prev <= k) return true;
        }
        return false;
    }
}
