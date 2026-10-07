import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] h = in.nextIntMatrix();
        IO.print(new Solution().trapRainWater(h));
    }
}
