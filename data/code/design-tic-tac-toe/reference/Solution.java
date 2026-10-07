import java.util.*;

class TicTacToe {
    private final int n;
    private final int[] rows, cols; // +1 for player 1, -1 for player 2
    private int diag, anti;

    public TicTacToe(int n) {
        this.n = n;
        rows = new int[n];
        cols = new int[n];
    }

    public int move(int row, int col, int player) {
        int d = player == 1 ? 1 : -1;
        rows[row] += d;
        cols[col] += d;
        if (row == col) diag += d;
        if (row + col == n - 1) anti += d;
        int target = d * n;
        if (rows[row] == target || cols[col] == target || diag == target || anti == target) return player;
        return 0;
    }
}
