import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int num = in.nextInt();
        IO.print(new Solution().encode(num));
    }
}
