import java.util.*;

class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length;
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < n - 2; i++) {
            if (a[i] > 0) break;
            if (i > 0 && a[i] == a[i - 1]) continue; // same first value already handled
            int lo = i + 1, hi = n - 1;
            while (lo < hi) {
                int s = a[i] + a[lo] + a[hi];
                if (s < 0) lo++;
                else if (s > 0) hi--;
                else {
                    out.add(Arrays.asList(a[i], a[lo], a[hi]));
                    while (lo < hi && a[lo] == a[lo + 1]) lo++;
                    while (lo < hi && a[hi] == a[hi - 1]) hi--;
                    lo++;
                    hi--;
                }
            }
        }
        return out;
    }
}
