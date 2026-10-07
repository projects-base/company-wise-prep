import java.util.*;

class Solution {
    private static final String[] KEYS = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

    public List<String> letterCombinations(String digits) {
        List<String> out = new ArrayList<>();
        if (digits.isEmpty()) return out;
        build(digits, 0, new StringBuilder(), out);
        return out;
    }

    private void build(String digits, int i, StringBuilder cur, List<String> out) {
        if (i == digits.length()) {
            out.add(cur.toString());
            return;
        }
        for (char c : KEYS[digits.charAt(i) - '0'].toCharArray()) {
            cur.append(c);
            build(digits, i + 1, cur, out);
            cur.deleteCharAt(cur.length() - 1);
        }
    }
}
