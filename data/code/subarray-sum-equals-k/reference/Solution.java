import java.util.*;

class Solution {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> seen = new HashMap<>(); // prefix sum -> how many times it occurred
        seen.put(0, 1);
        int sum = 0, count = 0;
        for (int x : nums) {
            sum += x;
            count += seen.getOrDefault(sum - k, 0);
            seen.merge(sum, 1, Integer::sum);
        }
        return count;
    }
}
