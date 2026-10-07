import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int source = in.nextInt();
        int target = in.nextInt();
        IO.print(new Solution().shortestPath(n, edges, source, target));
    }
}
