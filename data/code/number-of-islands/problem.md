You are given an `m x n` map where each cell is `'1'` (land) or `'0'` (water). An island is a group of land cells connected horizontally or vertically (not diagonally). Everything outside the map is water. Return the number of islands.

**Example 1**
Input: grid = [["1","1","0","0"],["1","1","0","0"],["0","0","1","0"],["0","0","0","1"]]
Output: 3
Why: the 2x2 block in the top-left is one island; the two single cells on the diagonal are separate islands because diagonal neighbours do not connect.

**Example 2**
Input: grid = [["1","1","1"],["0","1","0"],["1","1","1"]]
Output: 1

**Constraints**
- 1 ≤ m, n ≤ 300
- grid[i][j] is '0' or '1'

**Notes**: the hidden tests include a 150x150 map with long winding islands, so a recursive DFS may go very deep; BFS or an explicit stack is safer.
