import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String word1 = in.nextString();
        String word2 = in.nextString();
        IO.print(new Solution().minDistance(word1, word2));
    }
}
