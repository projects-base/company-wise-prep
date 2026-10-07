import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String start = in.nextString();
        String result = in.nextString();
        IO.print(new Solution().canTransform(start, result));
    }
}
