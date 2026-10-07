import java.util.*;

class Solution {
    public int characterReplacement(String s, int k) {
        int[] count = new int[26];
        int maxFreq = 0, best = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            maxFreq = Math.max(maxFreq, ++count[s.charAt(right) - 'A']);
            // The window never shrinks below its best size, so a stale maxFreq is harmless.
            while (right - left + 1 - maxFreq > k) count[s.charAt(left++) - 'A']--;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
