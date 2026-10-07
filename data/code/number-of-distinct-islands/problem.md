You are given an `m × n` grid of 0s (water) and 1s (land). An island is a group of land cells joined horizontally or vertically. Two islands have the same shape if one can be **slid** (translated) onto the other exactly. Rotations and reflections do **not** count: an L-shape and its mirror image are different shapes. Return the number of distinct island shapes.

**Example 1**
Input: grid = [[1,1,0,0,0],[1,1,0,0,0],[0,0,0,1,1],[0,0,0,1,1]]
Output: 1
Why: both islands are 2 × 2 squares.

**Example 2**
Input: grid = [[1,1,0,1,1],[1,0,0,0,0],[0,0,0,0,1],[1,1,0,1,1]]
Output: 3
Why: the top-left and bottom-right islands are mirror images of each other, so they count separately. The top-right and bottom-left islands are both a horizontal domino, so they share one shape.

**Constraints**
- 1 ≤ m, n ≤ 50
- grid[i][j] is 0 or 1

**Notes**: a grid with no land has 0 shapes.
