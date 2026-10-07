import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] envelopes = in.nextIntMatrix();
        IO.print(new Solution().maxEnvelopes(envelopes));
    }
}
