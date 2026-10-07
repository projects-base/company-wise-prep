import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] prices = in.nextIntArray();
        IO.print(new Solution().maxProfit(prices));
    }
}
