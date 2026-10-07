import java.util.*;

class Solution {
    public String makeGood(String s) {
        StringBuilder st = new StringBuilder(); // used as a stack
        for (char c : s.toCharArray()) {
            int n = st.length();
            if (n > 0 && st.charAt(n - 1) != c && Character.toLowerCase(st.charAt(n - 1)) == Character.toLowerCase(c)) {
                st.setLength(n - 1);
            } else {
                st.append(c);
            }
        }
        return st.toString();
    }
}
