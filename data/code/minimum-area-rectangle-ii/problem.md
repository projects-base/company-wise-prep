You are given a set of distinct points on the X-Y plane, `points[i] = [x, y]`. Find the smallest area of a rectangle whose four corners are all in the set. The rectangle may be **rotated**: its sides do not have to be parallel to the axes. Return 0 if no rectangle can be formed.

The output is printed with 5 decimal places; any answer within 10⁻⁵ of the true area prints the same.

**Example 1**
Input: points = [[1,2],[2,1],[1,0],[0,1]]
Output: 2.00000
Why: the four points form a square tilted by 45° with side √2.

**Example 2**
Input: points = [[0,1],[2,1],[1,1],[1,0],[2,0]]
Output: 1.00000
Why: (1,0), (1,1), (2,1), (2,0) form a unit square.

**Example 3**
Input: points = [[0,3],[1,2],[3,1],[1,3],[2,1]]
Output: 0.00000
Why: no four of these points form a rectangle.

**Constraints**
- 1 ≤ points.length ≤ 50
- 0 ≤ x, y ≤ 4·10⁴
- all points are distinct

**Notes**: two segments are the diagonals of a rectangle exactly when they share a midpoint and have equal length.
