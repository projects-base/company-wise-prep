**Short answer:** Reverse the flow. Instead of asking where water from each cell goes, start from each ocean's edge cells and walk *uphill* (to neighbours that are at least as high). Every cell reached that way can drain into that ocean. Do one BFS for the Pacific (top and left edges) and one for the Atlantic (bottom and right edges); the answer is the cells reached by both. O(m · n).

## Picture it

A small 3 × 3 island (Pacific on the top and left, Atlantic on the bottom and right):

```text
heights        Pacific reach   Atlantic reach   both
1 2 3          P P P           . . A            . . *
8 9 4          P P P           A A A            * * *
7 6 5          P P P           A A A            * * *
```

How each ocean climbs (multi-source BFS from its edge, stepping to a neighbour that is at least as high):

| Ocean | Starts (edge cells) | Climbs added | Not reached |
|---|---|---|---|
| Pacific | (0,0) 1, (0,1) 2, (0,2) 3, (1,0) 8, (2,0) 7 | (1,1) 9 from 2, (1,2) 4 from 3, (2,2) 5 from 4, (2,1) 6 from 5 | none |
| Atlantic | (2,0) 7, (2,1) 6, (2,2) 5, (0,2) 3, (1,2) 4 | (1,0) 8 from 7, (1,1) 9 from 4 | (0,0) 1, (0,1) 2: no path up from the Atlantic edge |

The answer is the 7 cells marked `*`. For example, rain on (0,1) (height 2) can only run down to (0,0) and the Pacific.

**The picture in one sentence:** instead of letting water run down from every cell, climb uphill from each ocean's edge once, and keep the cells both oceans reach.

## Approach

- **Brute force:** from every cell, search downhill to see which oceans it reaches. O((m · n)²) = 1.6·10⁹ for 200 × 200. Too slow.
- **Key insight:** "water flows from a to b if b is not higher" read backwards is "from b you can climb to a if a is not lower". All cells that drain into the Pacific are exactly the cells reachable by climbing from the Pacific's edge. That is one multi-source BFS per ocean.
- **Multi-source BFS:** put all edge cells of an ocean in the queue at the start, marked as reached. Then each cell is processed once per ocean.

## Solution

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class Solution {
    private static final int[] DR = {1, -1, 0, 0}, DC = {0, 0, 1, -1};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        boolean[][] pac = new boolean[m][n], atl = new boolean[m][n];
        ArrayDeque<int[]> pq = new ArrayDeque<>(), aq = new ArrayDeque<>();
        for (int r = 0; r < m; r++) { mark(pac, pq, r, 0); mark(atl, aq, r, n - 1); }
        for (int c = 0; c < n; c++) { mark(pac, pq, 0, c); mark(atl, aq, m - 1, c); }
        climb(heights, pac, pq);
        climb(heights, atl, aq);
        List<List<Integer>> out = new ArrayList<>();
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++)
                if (pac[r][c] && atl[r][c]) out.add(List.of(r, c));
        return out;
    }

    private static void mark(boolean[][] seen, ArrayDeque<int[]> q, int r, int c) {
        if (!seen[r][c]) { seen[r][c] = true; q.add(new int[] {r, c}); }
    }

    // BFS uphill: a neighbour at least as high can flow down into this cell.
    private static void climb(int[][] h, boolean[][] seen, ArrayDeque<int[]> q) {
        int m = h.length, n = h[0].length;
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + DR[d], k = c[1] + DC[d];
                if (r >= 0 && r < m && k >= 0 && k < n && !seen[r][k] && h[r][k] >= h[c[0]][c[1]]) {
                    seen[r][k] = true;
                    q.add(new int[] {r, k});
                }
            }
        }
    }
}
```

## Complexity

- **Time:** O(m · n): each BFS visits each cell at most once.
- **Space:** O(m · n) for the two reachability grids and the queues.

## Edge cases

- Single cell: touches both oceans.
- One row or one column: every cell touches both oceans (each cell is on a Pacific edge and an Atlantic edge).
- Flat plateaus: equal heights flow both ways, which is why the climb test is `>=`, not `>`.
- Corners: (0, n−1) and (m−1, 0) touch both oceans directly; `mark` avoids adding a cell twice to the same queue.
- Recursive DFS on a 200 × 200 grid can go 40,000 deep; BFS avoids that.

## Variations

- **Cells that reach at least one ocean:** OR instead of AND.
- **Trapping Rain Water II:** also starts from the border, but with a min-heap (a Dijkstra-like sweep) because the water level matters, not just reachability.
- **Surrounded Regions:** the same "start from the border" trick to find regions that cannot escape.

Practise it in the app: Run / Submit on this page.
