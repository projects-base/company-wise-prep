import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int k = new Solution().removeDuplicates(nums);
        IO.print(k);
        // Only the first k slots are judged.
        IO.print(Arrays.copyOf(nums, Math.max(0, Math.min(k, nums.length))));
    }
}
