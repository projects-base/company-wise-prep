import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] jugs = in.nextIntArray();
        int target = in.nextInt();
        IO.print(new Solution().canMeasureWater(jugs, target));
    }
}
