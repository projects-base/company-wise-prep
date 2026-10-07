import java.util.*;

class Solution {
    public boolean checkInclusion(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        if (n > m) return false;
        int[] diff = new int[26];
        for (int i = 0; i < n; i++) {
            diff[s1.charAt(i) - 'a']++;
            diff[s2.charAt(i) - 'a']--;
        }
        int nonZero = 0;
        for (int d : diff) if (d != 0) nonZero++;
        if (nonZero == 0) return true;
        for (int i = n; i < m; i++) {
            nonZero += change(diff, s2.charAt(i) - 'a', -1);
            nonZero += change(diff, s2.charAt(i - n) - 'a', +1);
            if (nonZero == 0) return true;
        }
        return false;
    }

    /** Applies delta to diff[c] and returns how the count of non-zero entries changed. */
    private int change(int[] diff, int c, int delta) {
        int before = diff[c] != 0 ? 1 : 0;
        diff[c] += delta;
        int after = diff[c] != 0 ? 1 : 0;
        return after - before;
    }
}
