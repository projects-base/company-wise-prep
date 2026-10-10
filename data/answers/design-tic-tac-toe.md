**Short answer:** Do not store or scan the board. Keep one counter per row, per column and for the two diagonals; add +1 for player 1's move and −1 for player 2's. A line is won when its counter reaches +n or −n, because that can only happen if one player owns all n cells. Each move updates at most four counters, so `move` is O(1) and memory is O(n).

## Picture it

Example 1, `n = 3` (player 1 adds +1, player 2 adds −1). Final board:

```text
      col 0  col 1  col 2
row 0   1      .      2
row 1   2      2      .
row 2   1      1      1    <- rows[2] reaches +3
```

| Move | (row, col, player) | Counters changed (new values) | Target | Returns |
|---|---|---|---|---|
| 1 | (0, 0, 1) | rows[0] = 1, cols[0] = 1, diag = 1 | +3 | 0 |
| 2 | (0, 2, 2) | rows[0] = 0, cols[2] = -1, anti = -1 | -3 | 0 |
| 3 | (2, 2, 1) | rows[2] = 1, cols[2] = 0, diag = 2 | +3 | 0 |
| 4 | (1, 1, 2) | rows[1] = -1, cols[1] = -1, diag = 1, anti = -2 | -3 | 0 |
| 5 | (2, 0, 1) | rows[2] = 2, cols[0] = 2, anti = -1 | +3 | 0 |
| 6 | (1, 0, 2) | rows[1] = -2, cols[0] = 1 | -3 | 0 |
| 7 | (2, 1, 1) | rows[2] = **3**, cols[1] = 0 | +3 | **1** |

Row 0 shows the trick: one cell from each player puts the counter at 0, so that line can never reach ±3.

**The picture in one sentence:** a signed counter per line hits ±n only when one player owns every cell in it, so each move just updates and checks its own row, column and diagonals.

## Approach

**Brute force.** Keep an n × n board. After each move, check the move's row, its column and, if relevant, the two diagonals: O(n) per move. That is already a fine first answer; a full-board scan (O(n²)) is the version to avoid.

**Key insight.** A move can only complete a line that passes through it: its row, its column, and a diagonal if it lies on one. We do not need to know which cells are owned, only whether all n cells in a line belong to the same player. Signed counting answers that: player 1 adds +1, player 2 adds −1. A line with any cell from each player can never reach ±n, and since moves are only on empty cells, reaching +n means player 1 has all n.

**Optimal.** Arrays `rows[n]`, `cols[n]` and two ints `diag` (cells with `row == col`) and `anti` (cells with `row + col == n − 1`).

## Solution

```java
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
```

Only the lines through this move are checked; checking `diag`/`anti` even when the move is off them is harmless, since they could not have just reached the target from this move.

## Complexity

- **Time:** O(1) per move.
- **Space:** O(n) — two arrays of n plus two ints, instead of an n² board.

## Edge cases

- The centre cell of an odd board lies on both diagonals; the two `if`s are independent, so both update.
- n = 2: any two marks in a line win; the counters handle it.
- The counter trick relies on "every move is on an empty cell". If moves can be invalid, keep a board (or a set of used cells) to reject repeats.

## Variations

- **A complete playable game (the prompt's `playGame`):** wrap this class in a loop that alternates players, reads a move, validates bounds and emptiness against a board, calls `move`, and stops on a win or after n² moves (a draw). Keep the referee (this class) separate from input/output so it is testable. See [E5 · The LLD interview method](../academy/lessons/E5.md).
- **Compare with a generated version:** typical weaknesses to look for are an O(n²) scan per move, hard-coded 3 × 3 checks, no validation of occupied cells, and missing draw detection.
- **Connect-K on an m × n board (Gomoku):** counters no longer work because a line is a window, not a whole row; after each move, walk out from the cell in the four directions and count consecutive same-player marks, O(K) per move.

Practise it in the app: Run / Submit on this page.
