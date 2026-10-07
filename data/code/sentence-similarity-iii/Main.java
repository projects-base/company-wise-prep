import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String a = in.nextString();
        String b = in.nextString();
        IO.print(new Solution().areSentencesSimilar(a, b));
    }
}
