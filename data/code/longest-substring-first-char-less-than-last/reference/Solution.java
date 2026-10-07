import java.util.*;

class Solution {
    public int longestSubstring(String s) {
        // For each end j, the best start is the earliest index holding any letter smaller than s[j].
        // first[c] = index of the first occurrence of letter c so far.
        int[] first = new int[26];
        Arrays.fill(first, Integer.MAX_VALUE);
        int best = 0;
        for (int j = 0; j < s.length(); j++) {
            int c = s.charAt(j) - 'a';
            int start = Integer.MAX_VALUE;
            for (int d = 0; d < c; d++) start = Math.min(start, first[d]);
            if (start != Integer.MAX_VALUE) best = Math.max(best, j - start + 1);
            if (first[c] == Integer.MAX_VALUE) first[c] = j;
        }
        return best;
    }
}
