import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] tasks = in.nextIntMatrix();
        IO.print(new Solution().getOrder(tasks));
    }
}
