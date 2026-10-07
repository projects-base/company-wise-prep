import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] flights = in.nextIntMatrix();
        int source = in.nextInt();
        int destination = in.nextInt();
        int startTime = in.nextInt();
        IO.print(new Solution().earliestArrival(n, flights, source, destination, startTime));
    }
}
