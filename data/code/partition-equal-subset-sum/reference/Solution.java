import java.util.*;

class Solution {
    public boolean canPartition(int[] nums) {
        int total = 0;
        for (int x : nums) total += x;
        if (total % 2 != 0) return false;
        int half = total / 2;
        boolean[] can = new boolean[half + 1]; // can[s]: some subset sums to s
        can[0] = true;
        for (int x : nums) {
            for (int s = half; s >= x; s--) {
                if (can[s - x]) can[s] = true;
            }
        }
        return can[half];
    }
}
