import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int k = in.nextInt();
        int[][] rowConditions = in.nextIntMatrix();
        int[][] colConditions = in.nextIntMatrix();
        int[][] m = new Solution().buildMatrix(k, rowConditions, colConditions);
        // Many matrices are correct, so verify instead of comparing cell by cell.
        if (m == null || m.length == 0) {
            IO.print(new int[0][]);
            return;
        }
        IO.print(check(k, m, rowConditions, colConditions));
    }

    private static String check(int k, int[][] m, int[][] rowConds, int[][] colConds) {
        if (m.length != k) return "invalid: expected " + k + " rows but got " + m.length;
        int[] row = new int[k + 1], col = new int[k + 1];
        Arrays.fill(row, -1);
        for (int r = 0; r < k; r++) {
            if (m[r] == null || m[r].length != k) return "invalid: row " + r + " does not have " + k + " columns";
            for (int c = 0; c < k; c++) {
                int v = m[r][c];
                if (v == 0) continue;
                if (v < 0 || v > k) return "invalid: cell (" + r + "," + c + ") holds " + v;
                if (row[v] != -1) return "invalid: number " + v + " appears more than once";
                row[v] = r;
                col[v] = c;
            }
        }
        for (int v = 1; v <= k; v++) if (row[v] == -1) return "invalid: number " + v + " is missing";
        for (int[] cond : rowConds) {
            if (row[cond[0]] >= row[cond[1]]) return "invalid: " + cond[0] + " is not above " + cond[1];
        }
        for (int[] cond : colConds) {
            if (col[cond[0]] >= col[cond[1]]) return "invalid: " + cond[0] + " is not left of " + cond[1];
        }
        return "valid";
    }
}
