import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        int alice = in.nextInt();
        int bob = in.nextInt();
        int dest = in.nextInt();
        int[] path = new Solution().shortestPath(n, edges, alice, dest);
        int[] nodes = new Solution().nodesOnShortestPaths(n, edges, alice, dest);
        boolean escape = new Solution().canEscape(n, edges, alice, bob, dest);
        IO.print(List.of(path, nodes, escape));
    }
}
