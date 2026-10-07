import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] parent = in.nextIntArray();
        int[] values = in.nextIntArray();
        IO.print(new Solution().maxGcdSum(parent, values));
    }
}
