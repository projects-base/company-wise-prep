Implement the referee for an `n x n` tic-tac-toe game between player 1 and player 2. A player wins as soon as they own all `n` cells of some row, some column, the main diagonal (top-left to bottom-right) or the anti-diagonal (top-right to bottom-left). Implement the class `TicTacToe`:

- `TicTacToe(int n)` sets up an empty `n x n` board.
- `int move(int row, int col, int player)` places a mark for `player` (1 or 2) at `(row, col)` and returns `player` if this move wins the game, otherwise `0`.

Every move is on an empty cell inside the board, and no moves are made after somebody has won.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor). The output is the list of return values, with `null` for the constructor.

**Example 1**
Input:
["TicTacToe","move","move","move","move","move","move","move"]
[[3],[0,0,1],[0,2,2],[2,2,1],[1,1,2],[2,0,1],[1,0,2],[2,1,1]]
Output: [null,0,0,0,0,0,0,1]
Why: player 1's last move completes the bottom row (2,0), (2,1), (2,2).

**Example 2**
Input:
["TicTacToe","move","move","move","move","move","move"]
[[3],[1,1,1],[0,0,2],[2,2,1],[1,0,2],[0,2,1],[2,0,2]]
Output: [null,0,0,0,0,0,2]
Why: player 2 owns the whole left column; player 1 never completes a line because (2,0) and (0,0) belong to player 2.

**Constraints**
- 2 ≤ n ≤ 100
- 0 ≤ row, col < n; player is 1 or 2
- at most n² calls to `move`

**Notes**: write your code in the class `TicTacToe` (keep that name). A straightforward board scan after each move is O(n) per move; try for O(1) per move by keeping per-row, per-column and per-diagonal counts. The hidden tests include a 60 x 60 game with thousands of moves.
