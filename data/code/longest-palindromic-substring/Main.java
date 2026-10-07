import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String s = in.nextString();
        String ans = new Solution().longestPalindrome(s);
        // Several answers can be correct: verify it is a palindromic substring, then print its length.
        if (ans == null || ans.isEmpty() || !s.contains(ans) || !isPalindrome(ans)) {
            System.out.println(IO.format(ans) + " is not a non-empty palindromic substring of s");
            return;
        }
        IO.print(ans.length());
    }

    private static boolean isPalindrome(String t) {
        for (int i = 0, j = t.length() - 1; i < j; i++, j--) {
            if (t.charAt(i) != t.charAt(j)) return false;
        }
        return true;
    }
}
