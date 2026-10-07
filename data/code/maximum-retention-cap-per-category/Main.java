import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] counts = in.nextIntArray();
        long maxEntries = in.nextLong();
        IO.print(new Solution().maxRetentionCap(counts, maxEntries));
    }
}
