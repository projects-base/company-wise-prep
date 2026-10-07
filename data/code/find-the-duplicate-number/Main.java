import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        int[] original = nums.clone();
        int ans = new Solution().findDuplicate(nums);
        if (!Arrays.equals(nums, original)) IO.print("array was modified");
        else IO.print(ans);
    }
}
