import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int[] friends = in.nextIntArray();
        IO.print(new Solution().minTotalCost(n, edges, friends));
    }
}
