You are given an `n × n` grid where `grid[r][c] = 1` marks a cell with a thief (or, in the interview's story, a cat) and `0` an empty cell. You start at the top-left cell `(0, 0)` and want to reach the bottom-right cell `(n − 1, n − 1)`, moving up, down, left or right. You may walk through any cell, including thieves' cells.

The **safeness** of a path is the smallest Manhattan distance (`|r1 − r2| + |c1 − c2|`) from any cell on the path — including the start and the end — to any thief. Return the **largest safeness** any path can have.

**Example 1**
Input: grid = [[1,0,0],[0,0,0],[0,0,1]]
Output: 0
Why: every path starts or ends on a thief's cell.

**Example 2**
Input: grid = [[0,0,1],[0,0,0],[0,0,0]]
Output: 2
Why: go down the left column and along the bottom row; the closest any of those cells gets to the thief at (0,2) is distance 2.

**Example 3**
Input: grid = [[0,0,0,1],[0,0,0,0],[0,0,0,0],[1,0,0,0]]
Output: 2

**Constraints**
- 1 ≤ n ≤ 400
- grid[r][c] is 0 or 1, and there is at least one thief

**Notes**: first compute every cell's distance to its nearest thief with a single multi-source BFS. Then you need the path whose *minimum* value is as large as possible — binary search on the answer, or a Dijkstra-style search that always expands the safest frontier cell.
