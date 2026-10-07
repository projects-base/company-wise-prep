import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int people = in.nextInt();
        int totalDays = in.nextInt();
        int[][] busy = in.nextIntMatrix();
        int minFree = in.nextInt();
        IO.print(new Solution().freeRanges(people, totalDays, busy, minFree));
    }
}
