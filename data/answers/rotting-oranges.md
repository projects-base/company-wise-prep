**Short answer:** Multi-source BFS. Put every rotten orange in the queue at minute 0 and count the fresh ones. Process the queue level by level; each level is one minute, and each fresh neighbour becomes rotten and joins the next level. Stop when no fresh oranges remain or the queue empties. If fresh oranges remain, return -1. O(m·n) time and space.

## Picture it

Example 1, `grid = [[2,1,1],[1,1,0],[0,1,1]]`. The number in each cell is the minute it rots (`R` = rotten at the start, `.` = empty):

```text
R 1 2
1 2 .
. 3 4
```

| Minute | Queue popped this level | Newly rotten (enqueued) | fresh left |
|---|---|---|---|
| start | – | (0,0) is the only source | 6 |
| 1 | (0,0) | (1,0), (0,1) | 4 |
| 2 | (1,0), (0,1) | (1,1), (0,2) | 2 |
| 3 | (1,1), (0,2) | (2,1) | 1 |
| 4 | (2,1) | (2,2) | 0 → loop stops, return 4 |

(0,2) rots nothing in minute 3: its only other neighbour (1,2) is empty. The loop stops as soon as `fresh` hits 0, so no extra empty minute is counted.

**The picture in one sentence:** start BFS from every rotten orange at once, and each BFS level is one minute, so the last level reached is the answer.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Rotting oranges: multi-source BFS, one level per minute","code":["int orangesRotting(int[][] g) {","  Deque<int[]> q = new ArrayDeque<>(); int fresh = 0;","  // every rotten orange starts in the queue; count the fresh ones","  int minutes = 0;","  while (!q.isEmpty() && fresh > 0) {","    for (int n = q.size(); n > 0; n--) {       // one BFS level = one minute","      int[] p = q.poll();","      for (int[] d : DIRS) {","        int r = p[0] + d[0], c = p[1] + d[1];","        if (inside(r, c) && g[r][c] == 1) { g[r][c] = 2; fresh--; q.add(new int[]{r, c}); }","      }","    }","    minutes++;","  }","  return fresh == 0 ? minutes : -1;","}"],"colors":{"2":"c4","1":"c3","0":"c5"},"frames":[
{"note":"Multi-source BFS: all rotten oranges (2) start in the queue at minute 0.","line":3,"grid":{"cells":[[2,1,1,0],[1,1,0,0],[0,1,1,1],[0,0,1,1]],"marks":{"0,0":"cur"}},"stack":{"kind":"queue","items":["(0,0)"]},"vars":{"minutes":0,"fresh":9}},
{"note":"Minute 1: the rot spreads one step from every orange in the queue; 2 more rotted, 7 fresh left.","line":13,"grid":{"cells":[[2,2,1,0],[2,1,0,0],[0,1,1,1],[0,0,1,1]],"marks":{"1,0":"cur","0,1":"cur"}},"stack":{"kind":"queue","items":["(1,0)","(0,1)"]},"vars":{"minutes":1,"fresh":7}},
{"note":"Minute 2: the rot spreads one step from every orange in the queue; 2 more rotted, 5 fresh left.","line":13,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,1,1,1],[0,0,1,1]],"marks":{"1,1":"cur","0,2":"cur"}},"stack":{"kind":"queue","items":["(1,1)","(0,2)"]},"vars":{"minutes":2,"fresh":5}},
{"note":"Minute 3: the rot spreads one step from every orange in the queue; 1 more rotted, 4 fresh left.","line":13,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,2,1,1],[0,0,1,1]],"marks":{"2,1":"cur"}},"stack":{"kind":"queue","items":["(2,1)"]},"vars":{"minutes":3,"fresh":4}},
{"note":"Minute 4: the rot spreads one step from every orange in the queue; 1 more rotted, 3 fresh left.","line":13,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,2,2,1],[0,0,1,1]],"marks":{"2,2":"cur"}},"stack":{"kind":"queue","items":["(2,2)"]},"vars":{"minutes":4,"fresh":3}},
{"note":"Minute 5: the rot spreads one step from every orange in the queue; 2 more rotted, 1 fresh left.","line":13,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,2,2,2],[0,0,2,1]],"marks":{"3,2":"cur","2,3":"cur"}},"stack":{"kind":"queue","items":["(3,2)","(2,3)"]},"vars":{"minutes":5,"fresh":1}},
{"note":"Minute 6: the rot spreads one step from every orange in the queue; 1 more rotted, 0 fresh left.","line":13,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,2,2,2],[0,0,2,2]],"marks":{"3,3":"cur"}},"stack":{"kind":"queue","items":["(3,3)"]},"vars":{"minutes":6,"fresh":0}},
{"note":"No fresh oranges left after 6 minutes. (If any fresh one were unreachable, the answer would be −1.)","line":15,"grid":{"cells":[[2,2,2,0],[2,2,0,0],[0,2,2,2],[0,0,2,2]]},"vars":{"minutes":6,"fresh":0}}
]}
```

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
