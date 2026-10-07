import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] intervals = in.nextIntMatrix();
        int[][] ans = new Solution().merge(intervals);
        // Any order is accepted, so print the intervals sorted.
        Arrays.sort(ans, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        IO.print(ans);
    }
}
