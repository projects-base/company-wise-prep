import java.util.*;

class Solution {
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> out = new ArrayList<>();
        int i = 0, n = words.length;
        while (i < n) {
            // Take words i..j-1 onto this line.
            int j = i + 1, len = words[i].length();
            while (j < n && len + 1 + words[j].length() <= maxWidth) {
                len += 1 + words[j].length();
                j++;
            }
            StringBuilder sb = new StringBuilder();
            int gaps = j - i - 1;
            if (j == n || gaps == 0) {
                for (int k = i; k < j; k++) {
                    if (k > i) sb.append(' ');
                    sb.append(words[k]);
                }
                while (sb.length() < maxWidth) sb.append(' ');
            } else {
                int letters = len - gaps; // total characters of the words alone
                int spaces = maxWidth - letters;
                int each = spaces / gaps, extra = spaces % gaps;
                for (int k = i; k < j; k++) {
                    sb.append(words[k]);
                    if (k < j - 1) {
                        int s = each + (k - i < extra ? 1 : 0);
                        for (int t = 0; t < s; t++) sb.append(' ');
                    }
                }
            }
            out.add(sb.toString());
            i = j;
        }
        return out;
    }
}
