import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] dungeon = in.nextIntMatrix();
        IO.print(new Solution().calculateMinimumHP(dungeon));
    }
}
