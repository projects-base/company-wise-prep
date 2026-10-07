You are given an `n x n` grid of 0s (water) and 1s (land). An island is a group of 1s connected up, down, left or right, and the grid contains **exactly two** islands. You may turn water cells into land. Return the smallest number of water cells you must turn into land so that the two islands become one connected island.

**Example 1**
Input: grid = [[0,1],[1,0]]
Output: 1
Why: flipping either 0 joins the two single-cell islands.

**Example 2**
Input: grid = [[0,1,0],[0,0,0],[0,0,1]]
Output: 2

**Example 3**
Input: grid = [[1,1,1,1,1],[1,0,0,0,1],[1,0,1,0,1],[1,0,0,0,1],[1,1,1,1,1]]
Output: 1
Why: the centre island is one water cell away from the surrounding ring.

**Constraints**
- 2 ≤ n ≤ 100
- grid[i][j] is 0 or 1
- there are exactly two islands
