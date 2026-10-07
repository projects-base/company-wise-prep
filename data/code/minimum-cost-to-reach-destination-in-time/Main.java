import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int maxTime = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int[] fees = in.nextIntArray();
        IO.print(new Solution().minCost(maxTime, edges, fees));
    }
}
