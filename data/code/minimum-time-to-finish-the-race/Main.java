import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] tires = in.nextIntMatrix();
        int changeTime = in.nextInt();
        int numLaps = in.nextInt();
        IO.print(new Solution().minimumFinishTime(tires, changeTime, numLaps));
    }
}
