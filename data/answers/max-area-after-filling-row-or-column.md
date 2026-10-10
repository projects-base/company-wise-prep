**Short answer:** Label the connected components once and record each one's size. If you fill row r, the new group is the C cells of that row plus every component that touches rows r−1, r, r+1, counted once each, minus the 1s that were already in row r (they are inside those components and in the row). Use a "stamp" array to count each component once per line. Do the same for columns. Total O(R · C).

## Picture it

Example 1: `grid = [[1,0,0],[0,0,1],[1,0,0]]`. Labelling finds three components of size 1: A at (0,0), B at (1,2), C at (2,0).

```text
A . .
. . B
C . .
```

Each line's total = line length + sizes of distinct components on or next to it − original 1s on the line.

| Line | Length | Components touched (once each) | 1s already on the line | Total |
|---|---|---|---|---|
| row 0 | 3 | A (on it), B (row 1) | 1 (A) | 3 + 2 − 1 = 4 |
| row 1 | 3 | A (row 0), B (on it), C (row 2) | 1 (B) | 3 + 3 − 1 = 5 |
| row 2 | 3 | B (row 1), C (on it) | 1 (C) | 3 + 2 − 1 = 4 |
| column 0 | 3 | A, C (both on it) | 2 | 3 + 2 − 2 = 3 |
| column 1 | 3 | A, C (column 0), B (column 2) | 0 | 3 + 3 − 0 = **6** |
| column 2 | 3 | B (on it) | 1 | 3 + 1 − 1 = 3 |

Best is 6: filling column 1 joins all three single cells into one group. The `stamp` array is what makes "once each" cheap: a component is added only the first time the current line's token is seen on it.

**The picture in one sentence:** label components once, then a filled line's group is just the line plus the distinct components touching it, minus the 1s it already had.

## Approach

- **Brute force:** for each of the R + C lines, copy the grid, fill the line, and flood-fill to find the largest group. O((R + C) · R · C). For a 1 × 10⁵ or 10⁵ × 1 grid that is 10¹⁰.
- **Key insight:** filling a line does not change any component that does not touch it. The only new group is the one containing the filled line, and it is the union of the line and every component adjacent to or on the line. So sizes from a single labelling pass are enough.
- **Avoid double counting:** a component can touch the line in many cells, so mark it with the current line's token. The original 1s inside row r are counted in their component's size and again in the C new cells, so subtract one for each.
- **Don't forget:** a large component far from every line is still a candidate. Take the max of all component sizes too.

## Solution

```java
import java.util.*;

class Solution {
    public int largestComponentAfterFill(int[][] grid) {
        int R = grid.length, C = grid[0].length;
        int[][] id = new int[R][C];
        for (int[] row : id) Arrays.fill(row, -1);
        List<Integer> size = new ArrayList<>();
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        int best = 0;
        // 1) label components with an iterative flood fill
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                if (grid[i][j] != 1 || id[i][j] >= 0) continue;
                int comp = size.size(), cnt = 0;
                id[i][j] = comp;
                stack.push(new int[] {i, j});
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    cnt++;
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < R && k >= 0 && k < C && grid[r][k] == 1 && id[r][k] < 0) {
                            id[r][k] = comp;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                size.add(cnt);
                best = Math.max(best, cnt);
            }
        }
        int[] stamp = new int[size.size()];
        int token = 0;
        // 2) each row: C new cells + distinct components in rows r-1..r+1 - original 1s in row r
        for (int r = 0; r < R; r++) {
            token++;
            int total = C;
            for (int rr = r - 1; rr <= r + 1; rr++) {
                if (rr < 0 || rr >= R) continue;
                for (int k = 0; k < C; k++) {
                    int c = id[rr][k];
                    if (c < 0) continue;
                    if (rr == r) total--;
                    if (stamp[c] != token) { stamp[c] = token; total += size.get(c); }
                }
            }
            best = Math.max(best, total);
        }
        // 3) each column, symmetric
        for (int k = 0; k < C; k++) {
            token++;
            int total = R;
            for (int kk = k - 1; kk <= k + 1; kk++) {
                if (kk < 0 || kk >= C) continue;
                for (int r = 0; r < R; r++) {
                    int c = id[r][kk];
                    if (c < 0) continue;
                    if (kk == k) total--;
                    if (stamp[c] != token) { stamp[c] = token; total += size.get(c); }
                }
            }
            best = Math.max(best, total);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(R · C). Labelling visits each cell once. Each row scan reads 3 rows (3C cells) and there are R rows; same for columns. Total about 7 · R · C.
- **Space:** O(R · C) for the labels and the flood-fill stack.

## Edge cases

- All zeros: the answer is the longer of R and C (filling a line of that length).
- All ones: R · C; the formula gives C + R·C − C.
- 1 × N or N × 1 grids: the "long, thin" case that kills the brute force; the stamp trick keeps it linear.
- Component touching the line from above and below: counted once thanks to the stamp.
- Recursive DFS on 10⁵ cells could overflow the stack; the explicit stack avoids that.

## Follow-up: avoiding O(R·C·(R+C))

That is exactly this solution: label once, then per line sum the distinct adjacent component sizes. A union-find labelling works just as well as flood fill. The stamp array (instead of a fresh `HashSet` per line) keeps each line's work proportional to the cells it reads, with no allocation.

Practise it in the app: Run / Submit on this page.
