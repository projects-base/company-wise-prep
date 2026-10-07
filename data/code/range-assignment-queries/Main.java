import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] arr = in.nextIntArray();
        int[][] queries = in.nextIntMatrix();
        IO.print(new Solution().applyAssignments(arr, queries));
    }
}
