import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] edges = in.nextIntMatrix();
        String colors = in.nextString();
        String pattern = in.nextString();
        List<Integer> ans = new ArrayList<>(new Solution().findRoots(n, edges, colors, pattern));
        // Any order is accepted, so print the roots sorted.
        Collections.sort(ans);
        IO.print(ans);
    }
}
