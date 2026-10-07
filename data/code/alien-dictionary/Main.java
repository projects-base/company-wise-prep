import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] words = in.nextStringArray();
        String[] copy = words.clone();
        String ans = new Solution().alienOrder(words);
        // Many alphabets can be correct, so check the answer instead of comparing it.
        if (ans == null || ans.isEmpty()) IO.print("");
        else if (valid(copy, ans)) IO.print("valid order");
        else IO.print("invalid order: " + ans);
    }

    static boolean valid(String[] words, String order) {
        int[] pos = new int[26];
        Arrays.fill(pos, -1);
        for (int i = 0; i < order.length(); i++) {
            int c = order.charAt(i) - 'a';
            if (c < 0 || c >= 26 || pos[c] != -1) return false;
            pos[c] = i;
        }
        boolean[] used = new boolean[26];
        for (String w : words) for (char ch : w.toCharArray()) used[ch - 'a'] = true;
        for (int c = 0; c < 26; c++) if (used[c] != (pos[c] != -1)) return false;
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i], b = words[i + 1];
            int j = 0;
            while (j < a.length() && j < b.length() && a.charAt(j) == b.charAt(j)) j++;
            if (j == a.length() || j == b.length()) {
                if (a.length() > b.length()) return false; // a word before its own prefix: no order works
                continue;
            }
            if (pos[a.charAt(j) - 'a'] > pos[b.charAt(j) - 'a']) return false;
        }
        return true;
    }
}
