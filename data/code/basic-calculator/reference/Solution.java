import java.util.*;

class Solution {
    public int calculate(String s) {
        // result/sign of the current parenthesis level; the stack saves outer levels.
        Deque<Long> stack = new ArrayDeque<>();
        long result = 0;
        int sign = 1;
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                long num = 0;
                while (i < n && Character.isDigit(s.charAt(i))) num = num * 10 + (s.charAt(i++) - '0');
                i--;
                result += sign * num;
            } else if (c == '+') {
                sign = 1;
            } else if (c == '-') {
                sign = -1; // binary minus and unary minus behave the same here
            } else if (c == '(') {
                stack.push(result);
                stack.push((long) sign);
                result = 0;
                sign = 1;
            } else if (c == ')') {
                long outerSign = stack.pop();
                long outerResult = stack.pop();
                result = outerResult + outerSign * result;
            }
        }
        return (int) result;
    }
}
