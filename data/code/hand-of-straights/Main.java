import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] hand = in.nextIntArray();
        int groupSize = in.nextInt();
        IO.print(new Solution().isNStraightHand(hand, groupSize));
    }
}
