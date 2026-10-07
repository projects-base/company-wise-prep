import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        List<String> ans = new ArrayList<>(new Solution().generateParenthesis(n));
        // Any order is accepted, so compare sorted.
        Collections.sort(ans);
        IO.print(ans);
    }
}
