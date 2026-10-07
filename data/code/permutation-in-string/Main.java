import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String s1 = in.nextString();
        String s2 = in.nextString();
        IO.print(new Solution().checkInclusion(s1, s2));
    }
}
