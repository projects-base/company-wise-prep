You are given a map `grid` where `1` is land and `0` is water. Everything outside the grid is open ocean. Land cells that touch **up, down, left or right** belong to the same island. You are also given a cell `(row, col)` that is land; it identifies one island.

A **lake** of that island is a maximal group of cells that cannot reach the ocean without crossing the chosen island. Water spreads only up, down, left and right — it cannot slip between two land cells that touch only at a corner. Other islands do **not** block water for this purpose: a small island sitting inside a lake is simply part of that lake, and water that is walled in only partly by the chosen island (and partly by some other island) is not a lake of the chosen island.

Return the number of lakes of the island containing `(row, col)`.

**Example 1**
Input: grid = [[1,1,1,1,1],[1,0,1,0,1],[1,1,1,1,1],[0,0,0,0,0]], row = 0, col = 0
Output: 2
Why: the two single water cells are each fully surrounded by the island.

**Example 2**
Input: grid = [[1,1,1,1,1,0],[1,0,0,0,1,0],[1,0,1,0,1,0],[1,0,0,0,1,0],[1,1,1,1,1,0],[0,0,0,0,0,0]], row = 0, col = 0
Output: 1
Why: the ring encloses one lake; the one-cell island at (2,2) sits inside that lake and does not split it.

**Example 3**
Input: grid = [[1,1,1],[1,0,1],[1,1,0]], row = 0, col = 0
Output: 1
Why: the centre water touches the corner water only diagonally, so it is trapped.

**Constraints**
- 1 ≤ rows, cols ≤ 250
- grid[i][j] is 0 or 1
- grid[row][col] = 1

**Notes**: the hidden tests include 250 × 250 grids with a single huge island, so very deep recursion may overflow the stack — an explicit queue or stack is safest.
