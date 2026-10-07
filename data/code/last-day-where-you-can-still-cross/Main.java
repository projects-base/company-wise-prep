import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int row = in.nextInt();
        int col = in.nextInt();
        int[][] cells = in.nextIntMatrix();
        IO.print(new Solution().latestDayToCross(row, col, cells));
    }
}
