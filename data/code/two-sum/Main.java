import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int target = in.nextInt();
        int[] ans = new Solution().twoSum(nums, target);
        // Any order is accepted, so print the pair sorted.
        Arrays.sort(ans);
        IO.print(ans);
    }
}
