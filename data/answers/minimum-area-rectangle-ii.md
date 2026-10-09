**Short answer:** Use the diagonals. Two segments are the diagonals of a rectangle exactly when they have the same midpoint and the same length. So group every pair of points by (midpoint, squared length). Any two pairs in the same group form a rectangle; compute its area with a cross product and keep the minimum. With n ≤ 50 that is about 1,225 pairs, so it is fast. Keep everything in integers by using doubled midpoints and squared lengths.

## Approach

- **Brute force:** try every 4 points and test whether they form a rectangle in any order. O(n⁴) = 6·10⁶ quadruples for n = 50, with several orderings each. It passes at this size but is messy.
- **Three points plus lookup:** pick p1, p2, p3 with a right angle at p1 (dot product 0), compute `p4 = p2 + p3 − p1`, and check it in a set. O(n³). Clean and correct.
- **Key insight (diagonals):** a quadrilateral is a parallelogram if its diagonals bisect each other (same midpoint), and a rectangle if, in addition, the diagonals have equal length. Grouping pairs by that key finds all rectangles at once.
- **Integer keys:** use `x1 + x2`, `y1 + y2` (twice the midpoint) and `dx² + dy²`. No floating point in the key, so no rounding mismatch.

## Solution

```java
import java.util.*;

class Solution {
    public double minAreaFreeRect(int[][] points) {
        Map<String, List<int[]>> groups = new HashMap<>();
        int n = points.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] a = points[i], b = points[j];
                long dx = a[0] - b[0], dy = a[1] - b[1];
                String key = (a[0] + b[0]) + "," + (a[1] + b[1]) + "," + (dx * dx + dy * dy);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(new int[] {i, j});
            }
        }
        long best = Long.MAX_VALUE;
        for (List<int[]> g : groups.values()) {
            for (int s = 0; s < g.size(); s++) {
                for (int t = s + 1; t < g.size(); t++) {
                    // diagonal (p1, p2) and diagonal (p3, p4): p3 and p4 are p1's neighbours
                    int[] p1 = points[g.get(s)[0]], p3 = points[g.get(t)[0]], p4 = points[g.get(t)[1]];
                    long ux = p3[0] - p1[0], uy = p3[1] - p1[1];
                    long vx = p4[0] - p1[0], vy = p4[1] - p1[1];
                    best = Math.min(best, Math.abs(ux * vy - uy * vx));   // |u x v| = side * side
                }
            }
        }
        return best == Long.MAX_VALUE ? 0.0 : (double) best;
    }
}
```

Why the cross product is the area: p3 and p4 are the two corners adjacent to p1, so `p3 − p1` and `p4 − p1` are the two sides, and the magnitude of their cross product is side × side. The area is an exact integer, which is why `long` works.

## Complexity

- **Time:** O(n²) to build groups, plus the pairs within groups. In the worst case (many concentric equal-length diagonals, such as points on a circle) that can approach O(n⁴), but with n ≤ 50 it stays small.
- **Space:** O(n²) for the groups.

## Edge cases

- Fewer than 4 points, or no rectangle: return 0.
- Axis-aligned rectangles are included; this solves both versions of the problem.
- Squares (Example 1): a square is a rectangle; same handling.
- Overflow: coordinates up to 4·10⁴ give squared lengths up to about 3.2·10⁹, beyond `int`, so compute `dx`, `dy` and products in `long`.
- Pairs in a group never share a point: two different diagonals with the same midpoint and length cannot share an endpoint (the other endpoint would then also coincide).

## Variations

- **Minimum Area Rectangle (axis-aligned):** for each pair as a diagonal, look up the other two corners in a set; O(n²).
- **Count rectangles:** sum `size · (size − 1) / 2` over the groups.
- **Squares only:** also require the diagonals to be perpendicular.

Practise it in the app: Run / Submit on this page.
