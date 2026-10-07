import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] jobs = in.nextIntArray();
        int d = in.nextInt();
        IO.print(new Solution().minDifficulty(jobs, d));
    }
}
