import java.util.*;

class Solution {
    public String redact(String text, String[] banned) {
        Set<String> ban = new HashSet<>();
        for (String w : banned) ban.add(w.toLowerCase(Locale.ROOT));
        StringBuilder out = new StringBuilder(text.length());
        int i = 0, n = text.length();
        while (i < n) {
            if (!isWordChar(text.charAt(i))) {
                out.append(text.charAt(i++));
                continue;
            }
            int j = i;
            while (j < n && isWordChar(text.charAt(j))) j++;
            String word = text.substring(i, j);
            out.append(ban.contains(word.toLowerCase(Locale.ROOT)) ? "REDACTED" : word);
            i = j;
        }
        return out.toString();
    }

    private static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }
}
