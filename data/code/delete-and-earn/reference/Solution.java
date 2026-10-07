import java.util.*;

class Solution {
    public int deleteAndEarn(int[] nums) {
        int max = 0;
        for (int x : nums) max = Math.max(max, x);
        int[] gain = new int[max + 1]; // total points from taking every copy of v
        for (int x : nums) gain[x] += x;
        // house robber over values: can't take both v and v - 1
        int take = 0, skip = 0;
        for (int v = 0; v <= max; v++) {
            int t = skip + gain[v];
            skip = Math.max(skip, take);
            take = t;
        }
        return Math.max(take, skip);
    }
}
