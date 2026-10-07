import java.util.*;

class Solution {
    public int calculate(String s) {
        // result = sum of finished terms; last = the term being built (may still be multiplied/divided)
        long result = 0, last = 0;
        long num = 0;
        char op = '+';
        int n = s.length();
        for (int i = 0; i <= n; i++) {
            char c = i < n ? s.charAt(i) : '+';
            if (c == ' ') continue;
            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
                continue;
            }
            switch (op) {
                case '+' -> { result += last; last = num; }
                case '-' -> { result += last; last = -num; }
                case '*' -> last = last * num;
                case '/' -> last = last / num; // Java truncates toward zero
            }
            op = c;
            num = 0;
        }
        return (int) (result + last);
    }
}
