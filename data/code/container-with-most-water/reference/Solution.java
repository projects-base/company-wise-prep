import java.util.*;

class Solution {
    public int maxArea(int[] height) {
        int l = 0, r = height.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, Math.min(height[l], height[r]) * (r - l));
            // moving the taller side can never help, so move the shorter one
            if (height[l] < height[r]) l++;
            else r--;
        }
        return best;
    }
}
