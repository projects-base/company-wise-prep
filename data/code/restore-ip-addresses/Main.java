import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String s = in.nextString();
        List<String> ans = new ArrayList<>(new Solution().restoreIpAddresses(s));
        // Any order is accepted, so print the list sorted.
        Collections.sort(ans);
        IO.print(ans);
    }
}
