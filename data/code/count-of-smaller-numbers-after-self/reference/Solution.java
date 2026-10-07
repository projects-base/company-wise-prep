import java.util.*;

class Solution {
    private static final int OFFSET = 10_001; // maps -10^4..10^4 to 1..20001

    // Walk from the right, keeping a Fenwick tree of how many times each value has been seen.
    public List<Integer> countSmaller(int[] nums) {
        int size = 2 * OFFSET + 1;
        int[] tree = new int[size + 1];
        Integer[] out = new Integer[nums.length];
        for (int i = nums.length - 1; i >= 0; i--) {
            int v = nums[i] + OFFSET;
            int smaller = 0;
            for (int k = v - 1; k > 0; k -= k & -k) smaller += tree[k];
            out[i] = smaller;
            for (int k = v; k <= size; k += k & -k) tree[k]++;
        }
        return Arrays.asList(out);
    }
}
