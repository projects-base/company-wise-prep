import java.util.*;

class Solution {
    public int findRadius(int[] houses, int[] heaters) {
        int[] h = houses.clone(), t = heaters.clone();
        Arrays.sort(h);
        Arrays.sort(t);
        int j = 0, best = 0;
        for (int x : h) {
            // advance to the heater closest to x (heaters are sorted, houses ascending)
            while (j + 1 < t.length && Math.abs((long) t[j + 1] - x) <= Math.abs((long) t[j] - x)) j++;
            best = Math.max(best, Math.abs(t[j] - x));
        }
        return best;
    }
}
