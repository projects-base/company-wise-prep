**Short answer:** The smallest rectangle is the bounding box of the 1s, so scan the grid once and track the smallest and largest row and column that contain a 1. The area is `(maxRow - minRow + 1) * (maxCol - minCol + 1)`, in O(rows · cols) time and O(1) space.

## Picture it

```text
      c0 c1 c2 c3 c4
 r0    0  0  0  0  0
 r1    0  0  1  0  0
 r2    0  1  0  1  0
 r3    0  0  0  0  0
```

Scan row by row; only the 1s change the bounds.

| 1 found at | minRow | maxRow | minCol | maxCol | box area |
|---|---|---|---|---|---|
| (1,2) | 1 | 1 | 2 | 2 | 1 |
| (2,1) | 1 | 2 | 1 | 2 | 4 |
| (2,3) | 1 | 2 | 1 | 3 | 6 |

Answer: rows 1 to 2 and columns 1 to 3, so 2 × 3 = 6.

**The picture in one sentence:** four numbers, the extreme rows and columns of the 1s, describe the whole answer.

## Approach

- **Assumption:** the rectangle's sides are horizontal and vertical, as on LeetCode.
- **Brute force:** try every rectangle and check that no 1 lies outside it. There are O(R²C²) rectangles, far too many.
- **Key insight:** any covering rectangle must reach the topmost, bottommost, leftmost and rightmost 1. The box through exactly those four is therefore the smallest, and it covers everything.
- **Optimal:** one pass updating four bounds.
- **Constant-factor speed-up:** scan inwards from each side and stop at the first 1. The worst case stays the same.

## Solution

```java
class Solution {
    public int minimumArea(int[][] grid) {
        int minRow = Integer.MAX_VALUE, maxRow = -1;
        int minCol = Integer.MAX_VALUE, maxCol = -1;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 1) {
                    minRow = Math.min(minRow, r);
                    maxRow = r;                       // rows are visited in increasing order
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }
        if (maxRow < 0) return 0;                     // no 1 in the grid
        return (maxRow - minRow + 1) * (maxCol - minCol + 1);
    }
}
```

## Complexity

- **Time:** O(R · C), each cell once. You cannot do better in general, because an unread cell could be a 1 outside your box.
- **Space:** O(1).

## Edge cases

- No 1 at all: LeetCode guarantees at least one; the code returns 0.
- A single 1: area 1. Forgetting the `+ 1` gives 0 here, the classic bug.
- All 1s in one row or column: the box is one cell thick.
- 1s only in opposite corners: the whole grid.
- With sides up to 1000 the area is at most 10⁶, so `int` is safe.

## Variations

- **Cover all ones with three rectangles (LC 3197):** split the grid in the six possible layouts and reuse this function on each part.
- **Smallest Rectangle Enclosing Black Pixels (LC 302):** the pixels are connected and one is given. Binary search each side for O(R log C + C log R).
- **Streaming 1s:** the four bounds update in O(1) per insert. Deletions need a count per row and column in sorted maps (`TreeMap`).
- **Largest rectangle of only 1s (LC 85):** a different problem, solved with histograms and a monotonic stack.

Related: [C4 · The optimisation playbook](../academy/lessons/C4.md).
