You are given a set of distinct points on the X-Y plane, `points[i] = [x, y]`. Find the smallest area of a rectangle whose four corners are all in the set and whose sides are parallel to the X and Y axes. Return 0 if no such rectangle exists.

**Example 1**
Input: points = [[1,1],[1,3],[3,1],[3,3],[2,2]]
Output: 4
Why: the corners (1,1), (1,3), (3,1), (3,3) form a 2 × 2 square.

**Example 2**
Input: points = [[1,1],[1,3],[3,1],[3,3],[4,1],[4,3]]
Output: 2
Why: (3,1), (3,3), (4,1), (4,3) give a 1 × 2 rectangle.

**Constraints**
- 1 ≤ points.length ≤ 500
- 0 ≤ x, y ≤ 4·10⁴
- all points are distinct

**Notes**: checking every group of four points (O(n⁴)) is too slow. Fix two points as a diagonal and look up the other two corners in a hash set.
