import java.util.*;

class Solution {
    public String longestPalindrome(String s) {
        int best = 0, start = 0;
        for (int c = 0; c < s.length(); c++) {
            for (int even = 0; even < 2; even++) {
                int l = c, r = c + even;
                while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
                    l--;
                    r++;
                }
                int len = r - l - 1;
                if (len > best) {
                    best = len;
                    start = l + 1;
                }
            }
        }
        return s.substring(start, start + best);
    }
}
