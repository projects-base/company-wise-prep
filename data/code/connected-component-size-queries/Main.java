import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int[] queries = in.nextIntArray();
        IO.print(new Solution().countReachable(n, edges, queries));
    }
}
