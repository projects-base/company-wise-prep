import java.util.*;

class Solution {
    public double[] medianSlidingWindow(int[] nums, int k) {
        // Two ordered sets of indices (ties broken by index): low holds the smaller half
        // (size ceil(k/2)), high the larger half.
        Comparator<Integer> cmp = (a, b) -> nums[a] != nums[b] ? Integer.compare(nums[a], nums[b]) : Integer.compare(a, b);
        TreeSet<Integer> low = new TreeSet<>(cmp), high = new TreeSet<>(cmp);
        double[] out = new double[nums.length - k + 1];
        for (int i = 0; i < nums.length; i++) {
            if (i >= k) {
                int old = i - k;
                if (!low.remove(old)) high.remove(old);
            }
            low.add(i);
            high.add(low.pollLast());
            while (high.size() > low.size()) low.add(high.pollFirst());
            if (i >= k - 1) {
                out[i - k + 1] = k % 2 == 1 ? nums[low.last()] : ((double) nums[low.last()] + nums[high.first()]) / 2.0;
            }
        }
        return out;
    }
}
