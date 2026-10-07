import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int k = in.nextInt();
        int[] ans = new Solution().topKFrequent(nums, k);
        // Any order is accepted, so print the values sorted.
        if (ans != null) Arrays.sort(ans);
        IO.print(ans);
    }
}
