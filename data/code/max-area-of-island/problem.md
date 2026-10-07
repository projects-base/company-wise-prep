You are given an `m × n` grid of 0s (water) and 1s (land). An island is a group of land cells joined horizontally or vertically (not diagonally), and its area is the number of cells in it. Return the largest island area in the grid, or 0 if there is no land.

**Example 1**
Input: grid = [[0,0,1,0,0],[0,1,1,0,0],[0,0,0,1,1],[1,0,0,1,1]]
Output: 4
Why: the island in the bottom-right corner has 4 cells. The others have 3 and 1.

**Example 2**
Input: grid = [[0,0,0,0,0,0,0,0]]
Output: 0

**Constraints**
- 1 ≤ m, n ≤ 300
- grid[i][j] is 0 or 1

**Notes**: one hidden test is a 300 × 300 grid holding a single snake-shaped island, so a recursive search must cope with very long paths. An iterative search is safest.
