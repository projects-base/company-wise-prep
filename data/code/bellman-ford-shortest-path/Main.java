import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int src = in.nextInt();
        IO.print(new Solution().shortestPaths(n, edges, src));
    }
}
