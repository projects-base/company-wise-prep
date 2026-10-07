import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        IO.print(new Solution().binarySearchableNumbers(nums));
    }
}
