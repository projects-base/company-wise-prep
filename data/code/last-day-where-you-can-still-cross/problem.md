A `row × col` field is all land on day 0. Each day one more cell floods: on day `i` (counting from 1) the cell `cells[i − 1] = [r, c]` turns into water, using **1-based** row and column numbers. Every cell floods exactly once, so by the last day the whole field is water.

You want to walk from **any** cell in the top row to **any** cell in the bottom row, stepping only on land and moving up, down, left or right. Return the **last day** on which such a walk is still possible (after that day's cell has flooded).

**Example 1**
Input: row = 2, col = 2, cells = [[1,1],[2,1],[1,2],[2,2]]
Output: 2
Why: after day 2 the right column is still land; on day 3 the top-right cell floods and the top row is all water.

**Example 2**
Input: row = 2, col = 2, cells = [[1,1],[1,2],[2,1],[2,2]]
Output: 1
Why: after day 1 you can walk down the right column; after day 2 the whole top row is water.

**Example 3**
Input: row = 3, col = 3, cells = [[1,2],[2,1],[3,3],[2,2],[1,1],[1,3],[2,3],[3,2],[3,1]]
Output: 3

**Constraints**
- 2 ≤ row, col ≤ 2 · 10⁴, and 4 ≤ row · col ≤ 2 · 10⁴
- cells.length = row · col, and all cells are distinct
- 1 ≤ r ≤ row, 1 ≤ c ≤ col

**Notes**: re-checking the whole grid after every day is O((row · col)²) and too slow for the hidden tests. Binary search on the day, or run time backwards and join land cells with union-find.

The same idea answers the related question "a tower is built each day — on which day do the first and last columns first become connected?": process the days forwards and stop at the first connection.
