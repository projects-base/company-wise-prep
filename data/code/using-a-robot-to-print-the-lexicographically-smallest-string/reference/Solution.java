import java.util.*;

class Solution {
    public String robotWithString(String s) {
        int n = s.length();
        // minFrom[i] = smallest character in s[i..n-1].
        char[] minFrom = new char[n + 1];
        minFrom[n] = (char) ('z' + 1);
        for (int i = n - 1; i >= 0; i--) minFrom[i] = (char) Math.min(s.charAt(i), minFrom[i + 1]);
        StringBuilder out = new StringBuilder(n);
        char[] stack = new char[n];
        int top = 0;
        for (int i = 0; i < n; i++) {
            stack[top++] = s.charAt(i);
            // Pop while the stack top is no larger than anything still waiting in s.
            while (top > 0 && stack[top - 1] <= minFrom[i + 1]) out.append(stack[--top]);
        }
        while (top > 0) out.append(stack[--top]);
        return out.toString();
    }
}
