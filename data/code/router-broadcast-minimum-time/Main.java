import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] routers = in.nextIntMatrix();
        int radius = in.nextInt();
        int source = in.nextInt();
        IO.print(new Solution().minTimeToTurnOn(routers, radius, source));
    }
}
