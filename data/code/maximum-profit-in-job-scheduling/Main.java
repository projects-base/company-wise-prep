import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] startTime = in.nextIntArray();
        int[] endTime = in.nextIntArray();
        int[] profit = in.nextIntArray();
        IO.print(new Solution().jobScheduling(startTime, endTime, profit));
    }
}
