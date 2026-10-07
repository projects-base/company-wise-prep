import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int minK = in.nextInt();
        int maxK = in.nextInt();
        IO.print(new Solution().countSubarrays(nums, minK, maxK));
    }
}
