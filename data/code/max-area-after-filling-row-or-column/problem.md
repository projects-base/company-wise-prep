You are given an `R × C` grid of 0s and 1s. Two 1-cells are connected when they share an edge (up, down, left, right). You must choose **exactly one** whole row or one whole column and set every cell in it to 1. Return the size of the largest connected group of 1s that can exist in the grid after that single change.

**Example 1**
Input: grid = [[1,0,0],[0,0,1],[1,0,0]]
Output: 6
Why: filling the middle column gives [[1,1,0],[0,1,1],[1,1,0]], where all 6 ones form a single group. Filling the middle row gives only 5, because the new row touches (0,0) and (2,0) but already contains (1,2).

**Example 2**
Input: grid = [[0,0],[0,0]]
Output: 2
Why: whatever line you fill, it becomes a group of 2.

**Example 3**
Input: grid = [[1,1,0,0],[1,1,0,0],[0,0,0,0],[0,0,1,1]]
Output: 10
Why: filling row 2 (0-based) adds 4 cells that touch both the 2 × 2 block above and the pair below, giving 4 + 4 + 2 = 10. Filling column 2 gives only 9: the block (4), three new cells, and the pair (2), one of whose cells is already in that column.

**Constraints**
- 1 ≤ R, C and R · C ≤ 10⁵
- grid[i][j] is 0 or 1

**Notes**: trying every line and running a fresh flood fill costs O(R·C·(R+C)), which is too slow for the long, thin hidden grids. Label the components once, then for each line add up the sizes of the **distinct** components that touch it.
