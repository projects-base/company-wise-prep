import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums1 = in.nextIntArray();
        int[] nums2 = in.nextIntArray();
        IO.print(new Solution().findMedianSortedArrays(nums1, nums2));
    }
}
