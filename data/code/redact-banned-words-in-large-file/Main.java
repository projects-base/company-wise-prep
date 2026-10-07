import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String text = in.nextString();
        String[] banned = in.nextStringArray();
        IO.print(new Solution().redact(text, banned));
    }
}
