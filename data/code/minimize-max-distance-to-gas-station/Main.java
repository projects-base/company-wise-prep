import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] stations = in.nextIntArray();
        int k = in.nextInt();
        double ans = new Solution().minmaxGasDist(stations, k);
        IO.print(ans);
    }
}
