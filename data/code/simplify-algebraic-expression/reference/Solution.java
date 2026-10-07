import java.util.*;

class Solution {
    public String simplify(String expression) {
        long[] coef = new long[26];
        // groupSign: the sign applied to everything inside the current parentheses.
        Deque<Integer> groupSign = new ArrayDeque<>();
        groupSign.push(1);
        int sign = 1;
        for (char c : expression.toCharArray()) {
            if (c == '+') sign = 1;
            else if (c == '-') sign = -1;
            else if (c == '(') { groupSign.push(groupSign.peek() * sign); sign = 1; }
            else if (c == ')') groupSign.pop();
            else { coef[c - 'a'] += (long) groupSign.peek() * sign; sign = 1; }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            long k = coef[i];
            if (k == 0) continue;
            if (k < 0) sb.append('-');
            else if (sb.length() > 0) sb.append('+');
            if (Math.abs(k) != 1) sb.append(Math.abs(k));
            sb.append((char) ('a' + i));
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}
