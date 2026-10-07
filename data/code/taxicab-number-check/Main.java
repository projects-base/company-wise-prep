import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        long n = in.nextLong();
        IO.print(new Solution().isTaxicab(n));
    }
}
