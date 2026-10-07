import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        char[][] grid = in.nextCharMatrix();
        IO.print(new Solution().numIslands(grid));
    }
}
