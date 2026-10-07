import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] patterns = in.nextStringArray();
        String[] queries = in.nextStringArray();
        IO.print(new Solution().matchIps(patterns, queries));
    }
}
