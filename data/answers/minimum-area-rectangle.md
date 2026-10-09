**Short answer:** Put every point in a hash set. Then treat each pair of points as a possible diagonal: if they differ in both x and y, the other two corners must be `(x1, y2)` and `(x2, y1)`. If both are in the set, you have a rectangle with area `|x1 − x2| · |y1 − y2|`. Keep the minimum (or the maximum, for the "largest" version). O(n²) pairs with O(1) lookups.

## Approach

- **Brute force:** every group of four points, checked for being an axis-parallel rectangle. O(n⁴) = 6·10¹⁰ for n = 500. Too slow.
- **Key insight:** an axis-parallel rectangle is fixed by one diagonal. Two opposite corners give the other two corners exactly, so you only enumerate pairs and look the rest up.
- **Encode a point as one `long`** (`x * 100000 + y`, safe because y < 10⁵) so the set holds primitives-as-keys without building strings.
- **Alternative:** group points by x column; for each pair of y values in a column, remember the last x where that (y1, y2) pair was seen. Same O(n²) in the worst case, often faster on sparse columns.

## Solution

```java
import java.util.HashSet;
import java.util.Set;

class Solution {
    public int minAreaRect(int[][] points) {
        Set<Long> set = new HashSet<>();
        for (int[] p : points) set.add(key(p[0], p[1]));
        long best = Long.MAX_VALUE;
        int n = points.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] a = points[i], b = points[j];
                if (a[0] == b[0] || a[1] == b[1]) continue;      // not a proper diagonal
                if (set.contains(key(a[0], b[1])) && set.contains(key(b[0], a[1]))) {
                    long area = (long) Math.abs(a[0] - b[0]) * Math.abs(a[1] - b[1]);
                    best = Math.min(best, area);
                }
            }
        }
        return best == Long.MAX_VALUE ? 0 : (int) best;
    }

    private static long key(int x, int y) {
        return (long) x * 100_000L + y;
    }
}
```

For the **largest** rectangle (the version reported in the interview), start `best` at 0 and use `Math.max`. Nothing else changes.

## Complexity

- **Time:** O(n²): about 125,000 pairs, each with two set lookups.
- **Space:** O(n) for the set.

## Edge cases

- Fewer than four points, or no rectangle: return 0.
- Pairs on the same row or column cannot be diagonals: skip them, otherwise you would look up the points themselves and get area 0.
- Each rectangle is found twice (once per diagonal); harmless for min or max.
- Area up to 4·10⁴ × 4·10⁴ = 1.6·10⁹: fits in `int`, but compute in `long` to be safe.
- Key collisions: `x * 100000 + y` is unique only because 0 ≤ y < 100000. For general coordinates use `((long) x << 32) | (y & 0xffffffffL)`.

## Follow-up: the rectangle need not be axis-parallel

Then one diagonal no longer fixes the other corners. Use the diagonal property instead: two segments are the diagonals of a rectangle exactly when they share a midpoint and have equal length. Group all pairs by (`x1 + x2`, `y1 + y2`, squared length), and any two pairs in a group form a rectangle; its area is the cross product of the two sides from one corner. See [Minimum Area Rectangle II](minimum-area-rectangle-ii.md).

## Variations

- **Count axis-parallel rectangles:** for each pair of y values per column, count how many earlier columns had the same pair; add that count.
- **Count squares:** also require `|dx| == |dy|`.

Practise it in the app: Run / Submit on this page.
