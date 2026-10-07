You are given a positive integer `k` and two lists of conditions:

- `rowConditions[i] = [above, below]`: the number `above` must be in a row strictly **above** the row of the number `below`;
- `colConditions[i] = [left, right]`: the number `left` must be in a column strictly **left** of the column of the number `right`.

Build a `k × k` matrix that contains each of the numbers `1` to `k` **exactly once**, with every other cell `0`, such that all conditions hold. If no such matrix exists, return an empty matrix (`new int[0][]`).

**Example 1**
Input: k = 3, rowConditions = [[1,2],[3,2]], colConditions = [[2,1],[3,2]]
Output: a valid matrix, for example [[3,0,0],[0,0,1],[0,2,0]]
Why: rows — 1 (row 1) and 3 (row 0) are above 2 (row 2); columns — 2 (col 1) is left of 1 (col 2) and 3 (col 0) is left of 2.

**Example 2**
Input: k = 3, rowConditions = [[1,2],[2,3],[3,1],[2,3]], colConditions = [[2,1]]
Output: []
Why: the row conditions form a cycle 1 above 2 above 3 above 1, which is impossible.

**Constraints**
- 2 ≤ k ≤ 400
- 1 ≤ rowConditions.length, colConditions.length ≤ 10⁴
- each condition has two different numbers in the range 1..k (conditions may repeat)

**Notes**: many matrices can be correct. The checker does not compare your matrix with a fixed answer — it verifies that your matrix is `k × k`, holds each of 1..k exactly once with zeros elsewhere, and satisfies every condition, then prints `"valid"`. If the conditions are contradictory, you must return the empty matrix, printed as `[]`.
