import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] houses = in.nextIntArray();
        int[] heaters = in.nextIntArray();
        IO.print(new Solution().findRadius(houses, heaters));
    }
}
