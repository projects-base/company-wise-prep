import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] values = in.nextIntArray();
        int[][] edges = in.nextIntMatrix();
        IO.print(new Solution().pathMexSums(values, edges));
    }
}
