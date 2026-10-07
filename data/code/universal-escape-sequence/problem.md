A grid is given as a list of equal-length rows, where `'.'` is an open cell, `'#'` is a wall and `'E'` is the single exit (also an open cell). A robot is placed on some open cell, but you do **not** know which one. You must produce one fixed sequence of moves (`'U'`, `'D'`, `'L'`, `'R'`) that brings the robot to the exit whichever open cell it started on.

- A move into a wall or off the grid does nothing: the robot stays where it is.
- As soon as the robot reaches `'E'` it leaves the grid, so the remaining moves do not matter for it.

Return such a sequence of at most **200,000** moves. If some open cell cannot reach the exit at all, return the empty string `""`. Any valid sequence is accepted, and it does not have to be the shortest. The judge simulates your sequence from every open cell and prints `true` if it works, or the first failing start position if it does not.

**Example 1**
Input: grid = ["E.",".."]
Output: true (for example with "UL")
Why: from (1,1), U goes to (0,1) and L to the exit. From (0,1), U is blocked and L reaches the exit. From (1,0), U reaches the exit.

**Example 2**
Input: grid = ["E#",".."]
Output: true (for example with "LU")

**Example 3**
Input: grid = ["E#.","##."]
Output: true only for ""
Why: the two cells on the right are walled off from the exit, so no sequence exists.

**Constraints**
- 1 ≤ rows, columns ≤ 20
- exactly one `'E'`; every other cell is `'.'` or `'#'`

**Notes**: think of all possible robot positions at once. Walking any one of them to the exit can only merge positions, never split them.
