You are given an `n x n` grid where `grid[i][j]` is the elevation of cell `(i, j)`. Rain makes the water level rise: at time `t` the water depth everywhere is `t`. You can swim from a cell to a 4-directionally adjacent cell only if the elevations of **both** cells are at most `t`, and swimming itself takes no time. Starting at the top-left cell `(0, 0)`, return the earliest time `t` at which you can reach the bottom-right cell `(n-1, n-1)`.

**Example 1**
Input: grid = [[0,2],[1,3]]
Output: 3
Why: the target cell has elevation 3, so nothing can happen before t = 3, and at t = 3 every cell is reachable.

**Example 2**
Input: grid = [[0,1,2,3,4],[24,23,22,21,5],[12,13,14,15,16],[11,17,18,19,20],[10,9,8,7,6]]
Output: 16
Why: the best route goes along the top row to 4, down to 5 and 16, left along the middle row to 12, down to 11 and 10, then right along the bottom row to 6. Its highest cell is 16.

**Constraints**
- 1 ≤ n ≤ 50
- every value in `grid` is distinct and lies in [0, n² − 1]

**Notes**: equivalently, find the path from corner to corner whose highest cell is as low as possible.
