import java.util.*;

class Solution {
    public int longestSubstring(String s, int k) {
        int n = s.length(), best = 0;
        // For each target number of distinct letters, slide a window that holds exactly that many.
        for (int target = 1; target <= 26; target++) {
            int[] cnt = new int[26];
            int distinct = 0, atLeastK = 0, left = 0;
            for (int right = 0; right < n; right++) {
                int c = s.charAt(right) - 'a';
                if (cnt[c]++ == 0) distinct++;
                if (cnt[c] == k) atLeastK++;
                while (distinct > target) {
                    int d = s.charAt(left++) - 'a';
                    if (cnt[d]-- == k) atLeastK--;
                    if (cnt[d] == 0) distinct--;
                }
                if (distinct == target && atLeastK == target) best = Math.max(best, right - left + 1);
            }
        }
        return best;
    }
}
