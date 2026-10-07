You have a grid with `n` rows (row `0` at the top) and `m` columns. You start at the bottom-left cell `(n − 1, 0)` and must reach the bottom-right cell `(n − 1, m − 1)`. From cell `(r, c)` you may move to `(r − 1, c + 1)` (diagonally up-right), `(r, c + 1)` (right) or `(r + 1, c + 1)` (diagonally down-right), as long as you stay inside the grid.

You are also given a list of `checkpoints`, each `[row, col]`. Count the paths that visit **every** checkpoint (with no checkpoints, count all paths). The order of visiting is not given separately — it is whatever order the path reaches them. Return the count modulo **1,000,000,007**.

**Example 1**
Input: n = 3, m = 4, checkpoints = []
Output: 4
Why: from (2,0) the path can stay low or climb; the four paths are R R R, R ↗ ↘, ↗ R ↘ and ↗ ↘ R.

**Example 2**
Input: n = 3, m = 4, checkpoints = [[1,1]]
Output: 2
Why: after ↗ to (1,1), either go R then ↘, or ↘ then R.

**Example 3**
Input: n = 3, m = 5, checkpoints = [[0,2]]
Output: 1
Why: the only way to touch the top row in column 2 is ↗ ↗ ↘ ↘.

**Constraints**
- 1 ≤ n, m ≤ 1000
- 0 ≤ checkpoints.length ≤ 10⁴
- 0 ≤ row < n, 0 ≤ col < m; checkpoints may repeat

**Notes**: think about how many cells of each column a single path can visit — it makes the checkpoints much easier to handle, and lets you work in O(n) extra space.
