**Short answer:** Mark the chosen island with a flood fill from `(row, col)`. Then treat every cell that is **not** on that island (water and other islands alike) as passable. Flood from every border cell that is not on the island: all of that reaches the ocean. The passable cells left unvisited are trapped by the chosen island, and each connected group of them is one lake. Three BFS passes, O(R·C).

## Picture it

Example 2, chosen island at `(0,0)`. Input on the left, `mark` after the three passes on the right (`1` = chosen island, `2` = reaches the ocean, `3` = lake):

```text
grid                 mark
1 1 1 1 1 0          1 1 1 1 1 2
1 0 0 0 1 0          1 3 3 3 1 2
1 0 1 0 1 0          1 3 3 3 1 2    <- (2,2) is land of another island,
1 0 0 0 1 0          1 3 3 3 1 2       so it is passable and joins the lake
1 1 1 1 1 0          1 1 1 1 1 2
0 0 0 0 0 0          2 2 2 2 2 2
```

| Pass | Start cells | What gets labelled | Result |
|---|---|---|---|
| 1. island | (0,0) | the 16 land cells of the ring, over land only | label 1 |
| 2. ocean | every unlabelled border cell: column 5 and row 5 | the 11 cells of column 5 and row 5; the ring stops the flood there | label 2 |
| 3. lakes | scan finds (1,1) unlabelled: `lakes = 1` | all 9 inner cells, including the land at (2,2) | label 3 |
| scan continues | no more cells with label 0 | – | answer **1** |

**The picture in one sentence:** make the chosen island the only wall, flood in from the border, and every leftover region is one lake.

## Approach

**Brute force.** For each water cell, BFS to see whether it can reach the border without crossing the chosen island, then group the trapped cells. O((R·C)²).

**Key insight.** Reverse the question. Instead of asking "can this cell escape?", flood once **from the ocean inwards**. The rules say only the chosen island blocks water — other islands do not — so the island is the only wall. Whatever the ocean flood cannot reach is enclosed by the chosen island.

**Optimal.**

1. BFS over land from `(row, col)` → label 1 (the chosen island).
2. For every border cell still unlabelled, BFS through unlabelled cells → label 2 (connected to the ocean).
3. Scan the grid. Each unlabelled cell starts a new lake: count it and BFS its region → label 3.

Moving only up/down/left/right everywhere is what makes Example 3 work: water cannot slip between two land cells that touch at a corner.

## Solution

```java
import java.util.*;

class Solution {
    private static final int[] DR = {1, -1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public int countLakes(int[][] grid, int row, int col) {
        int R = grid.length, C = grid[0].length;
        // 0 = unvisited, 1 = chosen island, 2 = reachable from the ocean, 3 = inside a lake
        int[][] mark = new int[R][C];

        // 1. Flood the chosen island over land cells.
        ArrayDeque<int[]> q = new ArrayDeque<>();
        mark[row][col] = 1;
        q.add(new int[] {row, col});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < R && c >= 0 && c < C && mark[r][c] == 0 && grid[r][c] == 1) {
                    mark[r][c] = 1;
                    q.add(new int[] {r, c});
                }
            }
        }

        // 2. Everything not on the island that touches the border can reach the ocean.
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if ((r == 0 || c == 0 || r == R - 1 || c == C - 1) && mark[r][c] == 0) flood(mark, r, c, 2);
            }
        }

        // 3. Each remaining non-island region is one lake.
        int lakes = 0;
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (mark[r][c] == 0) {
                    lakes++;
                    flood(mark, r, c, 3);
                }
            }
        }
        return lakes;
    }

    // Marks every unvisited cell reachable from (sr, sc) through unvisited cells.
    private static void flood(int[][] mark, int sr, int sc, int label) {
        int R = mark.length, C = mark[0].length;
        ArrayDeque<int[]> q = new ArrayDeque<>();
        mark[sr][sc] = label;
        q.add(new int[] {sr, sc});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < R && c >= 0 && c < C && mark[r][c] == 0) {
                    mark[r][c] = label;
                    q.add(new int[] {r, c});
                }
            }
        }
    }
}
```

Note that `flood` never looks at `grid`: once the chosen island is labelled 1, every other cell, water or land, is passable.

## Complexity

- **Time:** O(R·C) — each cell gets a label exactly once across the three passes.
- **Space:** O(R·C) for the label grid and the queue.

## Edge cases

- Island touching the border: fine, the border flood skips its cells.
- No enclosed pocket (a solid block, a straight line) → 0.
- Diagonal gaps (Example 3): water does not leak through corners, so the pocket is still a lake.
- 250 × 250 single island: a recursive DFS could go tens of thousands of frames deep and overflow the stack. Use an explicit `ArrayDeque`, as here.

## Follow-ups

- **What if there is land (another island) inside a lake?** Under these rules it does not split the lake: steps 2 and 3 flood straight through it, so the lake and its inner island count as one (Example 2). If the interviewer wants lakes made of water cells only, flood only through `grid == 0` in step 3; then an inner island that cuts the water in two produces two lakes. Clarify the rule before coding.
- **Lakes for every island:** label all islands, then repeat steps 2–3 per island, or flood the ocean once and attribute each enclosed region to the island that borders it.

See also [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).

Practise it in the app: Run / Submit on this page.
