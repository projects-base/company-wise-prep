import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String digits = in.nextString();
        List<String> ans = new ArrayList<>(new Solution().letterCombinations(digits));
        // Any order is accepted, so print the list sorted.
        Collections.sort(ans);
        IO.print(ans);
    }
}
