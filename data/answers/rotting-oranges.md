**Short answer:** Multi-source BFS. Put every rotten orange in the queue at minute 0 and count the fresh ones. Process the queue level by level; each level is one minute, and each fresh neighbour becomes rotten and joins the next level. Stop when no fresh oranges remain or the queue empties. If fresh oranges remain, return -1. O(m·n) time and space.

## Approach

- **Brute force.** Every minute, scan the whole grid and rot the neighbours of rotten cells. O((m·n)²) in the worst case: a winding path of 30,000 minutes on a 250×250 grid means 30,000 full scans of 62,500 cells.
- **Key insight.** The minute a fresh orange rots equals its shortest grid distance to the *nearest* rotten orange. Shortest distance in an unweighted grid is BFS. Starting BFS from all rotten cells at once (multi-source) computes the distance to the nearest source in one sweep, as if a virtual node connected to all of them.
- **Level-by-level.** Record the queue size at the start of each round; that many cells are processed this minute. Counting fresh oranges up front lets you answer -1 without a final scan.

## Solution

```java
import java.util.*;

class Solution {
    public int orangesRotting(int[][] grid) {
        int m = grid.length, n = grid[0].length, fresh = 0;
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 2) q.add(new int[] {i, j});
                else if (grid[i][j] == 1) fresh++;
            }
        int minutes = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (fresh > 0 && !q.isEmpty()) {
            minutes++;
            for (int s = q.size(); s > 0; s--) {
                int[] c = q.poll();
                for (int d = 0; d < 4; d++) {
                    int r = c[0] + dr[d], k = c[1] + dc[d];
                    if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1) {
                        grid[r][k] = 2;      // mark when enqueued, never twice
                        fresh--;
                        q.add(new int[] {r, k});
                    }
                }
            }
        }
        return fresh == 0 ? minutes : -1;
    }
}
```

The loop condition `fresh > 0` matters: without it you would count one extra minute for the last level, which rots nothing.

## Complexity

- **Time:** O(m·n). Each cell enters the queue at most once and checks four neighbours.
- **Space:** O(m·n) for the queue in the worst case. The grid itself serves as the visited set (it is mutated; copy it first if the caller needs it intact).

## Edge cases

- No fresh oranges at the start: 0, even if there are no rotten ones either.
- Fresh oranges but no rotten ones: -1.
- A fresh orange walled off by empty cells: -1.
- One row or one column.

## Variations

- **Walls and Gates / 01 Matrix:** the same multi-source BFS computing distance to the nearest source for every cell.
- **Diagonal spreading:** add four more direction pairs.
- **Different spread delays per cell:** BFS no longer works; use Dijkstra.

Practise it in the app: Run / Submit on this page.
