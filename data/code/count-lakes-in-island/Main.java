import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] grid = in.nextIntMatrix();
        int row = in.nextInt();
        int col = in.nextInt();
        IO.print(new Solution().countLakes(grid, row, col));
    }
}
