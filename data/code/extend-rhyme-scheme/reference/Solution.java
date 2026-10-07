import java.util.*;

class Solution {
    public String extendScheme(String shortScheme, String longScheme) {
        char[] longToShort = new char[26]; // 0 = not mapped yet
        char[] shortToLong = new char[26];
        int m = shortScheme.length(), n = longScheme.length();
        if (m > n) return "";
        // The overlapping prefix must be a one-to-one letter renaming.
        for (int i = 0; i < m; i++) {
            char s = shortScheme.charAt(i), l = longScheme.charAt(i);
            if (longToShort[l - 'A'] == 0 && shortToLong[s - 'A'] == 0) {
                longToShort[l - 'A'] = s;
                shortToLong[s - 'A'] = l;
            } else if (longToShort[l - 'A'] != s || shortToLong[s - 'A'] != l) {
                return "";
            }
        }
        StringBuilder sb = new StringBuilder(shortScheme);
        for (int i = m; i < n; i++) {
            char l = longScheme.charAt(i);
            if (longToShort[l - 'A'] == 0) {
                char fresh = 'A';
                while (shortToLong[fresh - 'A'] != 0) fresh++;
                longToShort[l - 'A'] = fresh;
                shortToLong[fresh - 'A'] = l;
            }
            sb.append(longToShort[l - 'A']);
        }
        return sb.toString();
    }
}
