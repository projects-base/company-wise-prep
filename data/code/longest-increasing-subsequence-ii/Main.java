import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int k = in.nextInt();
        IO.print(new Solution().lengthOfLIS(nums, k));
    }
}
