import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<List<Integer>> grid = in.nextIntListList();
        IO.print(new Solution().maximumSafenessFactor(grid));
    }
}
