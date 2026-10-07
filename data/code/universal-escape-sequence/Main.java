import java.util.*;

public class Main {
    static final int LIMIT = 200_000;

    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] grid = in.nextStringArray();
        String ans = new Solution().escapeSequence(grid.clone());
        IO.print(verdict(grid, ans));
    }

    /** Any valid sequence is accepted, so the judge checks the answer instead of comparing strings. */
    static Object verdict(String[] grid, String ans) {
        if (ans == null) return "returned null";
        int rows = grid.length, cols = grid[0].length();
        int exit = -1;
        List<Integer> open = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = grid[r].charAt(c);
                if (ch == 'E') exit = r * cols + c;
                if (ch != '#') open.add(r * cols + c);
            }
        }
        // Can every open cell reach the exit at all?
        boolean[] seen = new boolean[rows * cols];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        seen[exit] = true;
        q.add(exit);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (char m : "UDLR".toCharArray()) {
                int v = move(grid, u, m);
                if (!seen[v]) { seen[v] = true; q.add(v); }
            }
        }
        boolean possible = true;
        for (int cell : open) if (!seen[cell]) possible = false;
        if (!possible) return ans.isEmpty() ? (Object) true : "false: some cell cannot reach the exit, so the answer must be \"\"";

        if (ans.length() > LIMIT) return "false: the sequence is longer than " + LIMIT + " moves";
        for (char m : ans.toCharArray()) if ("UDLR".indexOf(m) < 0) return "false: invalid move '" + m + "'";
        for (int start : open) {
            int p = start;
            for (int i = 0; i < ans.length() && p != exit; i++) p = move(grid, p, ans.charAt(i));
            if (p != exit) {
                return "false: starting at row " + start / cols + ", col " + start % cols
                        + " you end at row " + p / cols + ", col " + p % cols;
            }
        }
        return true;
    }

    static int move(String[] grid, int u, char m) {
        int cols = grid[0].length();
        int r = u / cols, c = u % cols;
        if (m == 'U') r--;
        else if (m == 'D') r++;
        else if (m == 'L') c--;
        else c++;
        if (r < 0 || r >= grid.length || c < 0 || c >= cols || grid[r].charAt(c) == '#') return u;
        return r * cols + c;
    }
}
