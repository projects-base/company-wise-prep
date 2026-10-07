import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] houses = in.nextIntMatrix();
        int costPerHeight = in.nextInt();
        int payPerHouse = in.nextInt();
        IO.print(new Solution().maxProfit(houses, costPerHeight, payPerHouse));
    }
}
