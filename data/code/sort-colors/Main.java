import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        new Solution().sortColors(nums);
        IO.print(nums);
    }
}
