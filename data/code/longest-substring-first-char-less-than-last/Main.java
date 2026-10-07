import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String s = in.nextString();
        IO.print(new Solution().longestSubstring(s));
    }
}
