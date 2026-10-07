You are given an `m × n` grid `heightMap` where each cell is a column of unit-square blocks of the given height. After heavy rain, water collects on top of the columns wherever it is enclosed by higher walls; water can flow between cells that share an edge, and anything that reaches the outer border of the grid runs off. Return the total volume of water that stays trapped.

**Example 1**
Input: heightMap = [[1,4,3,1,3,2],[3,2,1,3,2,4],[2,3,3,2,3,1]]
Output: 4
Why: in the middle row, the cells of height 2 and 1 on the left fill up to level 3 (1 + 2 units) and the cell of height 2 on the right fills to level 3 (1 unit): 4 in total.

**Example 2**
Input: heightMap = [[3,3,3,3,3],[3,2,2,2,3],[3,2,1,2,3],[3,2,2,2,3],[3,3,3,3,3]]
Output: 10
Why: the inner 3×3 bowl fills to level 3: eight cells hold 1 each and the centre holds 2.

**Constraints**
- 1 ≤ m, n ≤ 200
- 0 ≤ heightMap[i][j] ≤ 2 × 10⁴

**Notes**: border cells can never hold water. Expect grids of 200 × 200 in the hidden tests, so repeated full-grid flood passes are too slow; aim for O(mn log(mn)).
