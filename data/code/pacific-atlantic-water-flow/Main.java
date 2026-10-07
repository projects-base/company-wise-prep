import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[][] heights = in.nextIntMatrix();
        List<List<Integer>> ans = new Solution().pacificAtlantic(heights);
        // Any order is accepted, so print the cells sorted by row, then column.
        List<List<Integer>> sorted = new ArrayList<>();
        if (ans != null) for (List<Integer> cell : ans) sorted.add(new ArrayList<>(cell));
        sorted.sort((a, b) -> {
            for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
                int c = Integer.compare(a.get(i), b.get(i));
                if (c != 0) return c;
            }
            return Integer.compare(a.size(), b.size());
        });
        IO.print(sorted);
    }
}
