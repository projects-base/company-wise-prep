import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int m = in.nextInt();
        int n = in.nextInt();
        IO.print(new Solution().numberOfPatterns(m, n));
    }
}
