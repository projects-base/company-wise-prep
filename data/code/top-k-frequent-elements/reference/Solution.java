import java.util.*;

class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int x : nums) count.merge(x, 1, Integer::sum);
        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i <= nums.length; i++) buckets.add(new ArrayList<>());
        for (var e : count.entrySet()) buckets.get(e.getValue()).add(e.getKey());
        int[] out = new int[k];
        int at = 0;
        for (int f = nums.length; f >= 1 && at < k; f--) {
            for (int x : buckets.get(f)) {
                if (at < k) out[at++] = x;
            }
        }
        return out;
    }
}
