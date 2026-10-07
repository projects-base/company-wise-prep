import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String s = in.nextString();
        String target = in.nextString();
        IO.print(new Solution().containsNumber(s, target));
    }
}
