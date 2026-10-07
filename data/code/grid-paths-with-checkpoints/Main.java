import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int m = in.nextInt();
        int[][] checkpoints = in.nextIntMatrix();
        IO.print(new Solution().countPaths(n, m, checkpoints));
    }
}
