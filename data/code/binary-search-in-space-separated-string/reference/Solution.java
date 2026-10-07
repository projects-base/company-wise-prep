import java.util.*;

class Solution {
    public boolean containsNumber(String s, String target) {
        // Search window [lo, hi) of character positions; it always starts and ends on number boundaries.
        int lo = 0, hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            // expand to the number containing mid (if mid is a space, this picks the number just before it)
            int start = mid, end = mid;
            while (start > lo && s.charAt(start - 1) != ' ') start--;
            while (end < hi && s.charAt(end) != ' ') end++;
            int cmp = compare(s, start, end, target);
            if (cmp == 0) return true;
            if (cmp < 0) lo = end + 1; // skip this number and the space after it
            else hi = start - 1;       // keep only numbers before it (drop the space)
        }
        return false;
    }

    /** Compares s[start, end) with target as non-negative integers without leading zeros. */
    private int compare(String s, int start, int end, String target) {
        int len = end - start;
        if (len != target.length()) return Integer.compare(len, target.length());
        for (int i = 0; i < len; i++) {
            char a = s.charAt(start + i), b = target.charAt(i);
            if (a != b) return Character.compare(a, b);
        }
        return 0;
    }
}
