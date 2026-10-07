import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] tokens = in.nextIntArray();
        IO.print(new Solution().firstPlayerWins(tokens));
    }
}
