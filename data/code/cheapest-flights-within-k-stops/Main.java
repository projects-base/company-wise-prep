import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] flights = in.nextIntMatrix();
        int src = in.nextInt();
        int dst = in.nextInt();
        int k = in.nextInt();
        IO.print(new Solution().findCheapestPrice(n, flights, src, dst, k));
    }
}
