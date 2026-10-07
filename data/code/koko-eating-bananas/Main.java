import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] piles = in.nextIntArray();
        int h = in.nextInt();
        IO.print(new Solution().minEatingSpeed(piles, h));
    }
}
