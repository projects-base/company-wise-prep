Given an `m × n` matrix, return all of its elements in spiral order: start at the top-left corner, go right along the top row, down the right column, left along the bottom row, up the left column, and keep spiralling inwards until every element has been visited exactly once.

**Example 1**
Input: matrix = [[1,2,3],[4,5,6],[7,8,9]]
Output: [1,2,3,6,9,8,7,4,5]

**Example 2**
Input: matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]]
Output: [1,2,3,4,8,12,11,10,9,5,6,7]

**Constraints**
- 1 ≤ m, n ≤ 10
- −100 ≤ matrix[i][j] ≤ 100

**Notes**: in the interview this was asked "LLD style" — model it with classes such as `Matrix`, a `TraversalStrategy` interface and a `SpiralTraversal` implementation, so a new traversal (zigzag, diagonal) can be added without changing existing code. You may declare those extra classes (without `public`) in the same file as `Solution`; the checker only calls `spiralOrder`. Watch single-row and single-column matrices.
