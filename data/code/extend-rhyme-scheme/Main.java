import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String shortScheme = in.nextString();
        String longScheme = in.nextString();
        IO.print(new Solution().extendScheme(shortScheme, longScheme));
    }
}
