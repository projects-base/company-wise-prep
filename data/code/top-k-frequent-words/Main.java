import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] words = in.nextStringArray();
        int k = in.nextInt();
        IO.print(new Solution().topKFrequent(words, k));
    }
}
