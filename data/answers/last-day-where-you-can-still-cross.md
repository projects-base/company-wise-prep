**Short answer:** Flooding removes cells, and union-find cannot split sets, so run time backwards: start with all water and add cells back as land, from the last day to the first. Add two virtual nodes, "top row" and "bottom row". When a cell becomes land, union it with land neighbours (and with top or bottom if it is on that row). The first moment, going backwards, that top and bottom are connected is the day whose cell you just restored, so the answer is that day minus one. O(N · α(N)).

## Picture it

Example 3: `row = 3`, `col = 3`, `cells = [[1,2],[2,1],[3,3],[2,2],[1,1],[1,3],[2,3],[3,2],[3,1]]`. Start with all water (`W`) and restore cells from day 9 backwards (`L` = land).

| Day undone | Cell restored | Unions | Grid after (rows 1 / 2 / 3) | top connected to bottom? |
|---|---|---|---|---|
| 9 | (3,1) | bottom | WWW / WWW / LWW | no |
| 8 | (3,2) | bottom, (3,1) | WWW / WWW / LLW | no |
| 7 | (2,3) | none (no land neighbour) | WWW / WWL / LLW | no |
| 6 | (1,3) | top, (2,3) | WWL / WWL / LLW | no |
| 5 | (1,1) | top | LWL / WWL / LLW | no |
| 4 | (2,2) | (3,2), (2,3) | LWL / WLL / LLW | **yes**: return 4 − 1 = 3 |

When (2,2) comes back it joins the top group {(1,3), (2,3)} with the bottom group {(3,1), (3,2)}, so the path (1,3) → (2,3) → (2,2) → (3,2) exists in the grid as it was after day 3.

**The picture in one sentence:** run the flood backwards so cells only ever appear, and union-find with virtual top and bottom nodes tells you the moment a crossing first exists.

## Approach

- **Brute force:** for each day, flood the cell and run BFS from the top row. O(N) days × O(N) BFS = O(N²) with N = row · col = 2·10⁴. That is 4·10⁸, too slow.
- **Binary search on the day:** "can I cross after day d?" is monotonic (more water never helps). Binary search d and run one BFS per check: O(N log N). Good and simple.
- **Key insight for union-find:** union-find only *adds* connections. Reversed time turns "cells disappear" into "cells appear", which only adds connections. Two virtual nodes turn "is any top cell connected to any bottom cell" into one `find` comparison.
- **Optimal:** reverse union-find, O(N · α(N)).

## Solution

```java
class Solution {
    private int[] parent;

    public int latestDayToCross(int row, int col, int[][] cells) {
        int n = row * col, top = n, bottom = n + 1;
        parent = new int[n + 2];
        for (int i = 0; i < n + 2; i++) parent[i] = i;
        boolean[] land = new boolean[n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        for (int day = cells.length; day >= 1; day--) {
            int r = cells[day - 1][0] - 1, c = cells[day - 1][1] - 1, id = r * col + c;
            land[id] = true;                          // undo this day's flood
            if (r == 0) union(id, top);
            if (r == row - 1) union(id, bottom);
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr >= 0 && nr < row && nc >= 0 && nc < col && land[nr * col + nc]) {
                    union(id, nr * col + nc);
                }
            }
            // The grid now looks like "after day - 1": crossable.
            if (find(top) == find(bottom)) return day - 1;
        }
        return 0;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];  // path halving
            x = parent[x];
        }
        return x;
    }

    private void union(int a, int b) { parent[find(a)] = find(b); }
}
```

## Complexity

- **Time:** O(N · α(N)) with path compression; effectively linear. (Without union by rank it is O(N log N) in the worst case, still fine.)
- **Space:** O(N) for `parent` and `land`.

## Edge cases

- 1-based input: convert to 0-based before computing ids.
- `row == 2`: every land cell is on the top or bottom row; a cell on both would only happen with row 1, which the constraints exclude.
- Answer 0: if no crossing exists even after day 0 is reached, which cannot happen here (day 0 is all land) but the fallback keeps the method total.
- Id formula is `r * col + c`, not `r * row + c`. A classic bug on non-square grids.

## Follow-up: a tower is built each day; when do the first and last columns first connect?

Now cells are *added*, so union-find runs forwards, no reversal. Create virtual nodes "left" and "right". Each day, mark the tower, union it with neighbouring towers (4- or 8-directional; ask which, since 8 matters for "walls" that block a path), union it with "left" if `c == 0` and "right" if `c == col - 1`. Return the first day `find(left) == find(right)`. As an API, `addTower(r, c)` returns whether they are connected now, O(α(N)) per call.

This also shows the duality: the last day you can cross top-to-bottom on land equals the day before the flooded cells form an 8-connected left-to-right wall.

Practise it in the app: Run / Submit on this page.
