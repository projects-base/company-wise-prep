import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] logs = in.nextIntMatrix();
        int n = in.nextInt();
        IO.print(new Solution().earliestAcq(logs, n));
    }
}
