import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] words = in.nextStringArray();
        int maxWidth = in.nextInt();
        IO.print(new Solution().fullJustify(words, maxWidth));
    }
}
