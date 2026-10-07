import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int numCourses = in.nextInt();
        int[][] prerequisites = in.nextIntMatrix();
        int[][] copy = new int[prerequisites.length][];
        for (int i = 0; i < copy.length; i++) copy[i] = prerequisites[i].clone();
        int[] ans = new Solution().findOrder(numCourses, prerequisites);
        // Many orders can be correct, so check the answer instead of comparing it.
        if (ans == null || ans.length == 0) {
            IO.print(new int[0]);
        } else if (valid(numCourses, copy, ans)) {
            IO.print("valid order");
        } else {
            IO.print("invalid order: " + IO.format(ans));
        }
    }

    static boolean valid(int n, int[][] pre, int[] order) {
        if (order.length != n) return false;
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int c = order[i];
            if (c < 0 || c >= n || pos[c] != -1) return false;
            pos[c] = i;
        }
        for (int[] p : pre) if (pos[p[1]] > pos[p[0]]) return false;
        return true;
    }
}
