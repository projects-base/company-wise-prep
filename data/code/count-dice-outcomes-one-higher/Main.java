import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int k = in.nextInt();
        IO.print(new Solution().countOutcomes(n, k));
    }
}
