An island is an `m × n` grid of cell heights, `heights[r][c]`. The Pacific Ocean touches the top and left edges of the island, and the Atlantic Ocean touches the bottom and right edges. Rain on a cell can flow to a neighbouring cell (up, down, left, right) whose height is **less than or equal to** the current cell's height. Water flows into an ocean from any cell on an edge that ocean touches. Return the coordinates `[r, c]` of every cell from which water can reach **both** oceans.

Return the cells in any order. They are printed sorted by row, then by column.

**Example 1**
Input: heights = [[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]
Output: [[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]
Why: for example, from (2,2) (height 5) water can go up and left to the Pacific, and right and down to the Atlantic.

**Example 2**
Input: heights = [[1]]
Output: [[0,0]]
Why: the single cell touches both oceans.

**Constraints**
- 1 ≤ m, n ≤ 200
- 0 ≤ heights[r][c] ≤ 10⁵

**Notes**: running a separate search from every cell is O((mn)²). Search uphill from each ocean's edges instead. One hidden test is a 200 × 200 grid.
