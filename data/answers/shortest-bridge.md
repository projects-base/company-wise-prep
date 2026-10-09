**Short answer:** Find one island with a flood fill and put all of its cells into a queue at distance 0. Then run a multi-source BFS outward over water. The first time the BFS touches a land cell that is not part of the first island, the distance of the cell you came from is the number of water cells to flip. Everything is O(n²).

## Approach

- **Brute force:** for every cell of island A and every cell of island B, take the Manhattan distance minus 1, and keep the minimum. That is O(|A|·|B|), up to O(n⁴). It is actually correct here (water paths are unobstructed between two islands), but too slow for n = 100 in the worst case.
- **Key insight:** "fewest water cells to connect" is a shortest path where land costs 0 and water costs 1. If you start BFS from **every** cell of island A at once, the BFS layers grow outward evenly, and layer k is all water at distance k from A.
- **Optimal:**
  1. Scan for the first `1`, flood-fill its island (iterative, to avoid stack overflow on a 100×100 island), marking each cell with distance 0 and adding it to the BFS queue.
  2. BFS. When you pop cell c and see an unvisited neighbour: if it is land, it belongs to island B, so return `dist[c]`. Otherwise give it `dist[c] + 1` and enqueue it.

## Solution

```java
import java.util.*;

class Solution {
    public int shortestBridge(int[][] grid) {
        int n = grid.length;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>();
        // 1. Flood-fill the first island; every cell becomes a BFS source at distance 0.
        outer:
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    ArrayDeque<int[]> stack = new ArrayDeque<>();
                    stack.push(new int[] {i, j});
                    dist[i][j] = 0;
                    while (!stack.isEmpty()) {
                        int[] c = stack.pop();
                        q.add(c);
                        for (int d = 0; d < 4; d++) {
                            int r = c[0] + dr[d], k = c[1] + dc[d];
                            if (r >= 0 && r < n && k >= 0 && k < n
                                    && grid[r][k] == 1 && dist[r][k] == -1) {
                                dist[r][k] = 0;
                                stack.push(new int[] {r, k});
                            }
                        }
                    }
                    break outer;
                }
            }
        }
        // 2. Multi-source BFS over water until we touch the second island.
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + dr[d], k = c[1] + dc[d];
                if (r < 0 || r >= n || k < 0 || k >= n || dist[r][k] != -1) continue;
                if (grid[r][k] == 1) return dist[c[0]][c[1]];   // unvisited land = island B
                dist[r][k] = dist[c[0]][c[1]] + 1;
                q.add(new int[] {r, k});
            }
        }
        return -1;   // unreachable when there are exactly two islands
    }
}
```

## Complexity

- **Time:** O(n²). Each cell is visited at most once by the flood fill and once by the BFS.
- **Space:** O(n²) for `dist` and the queue.

## Edge cases

- Islands diagonally adjacent (`[[0,1],[1,0]]`): answer 1, since diagonals do not connect.
- One island surrounding the other (example 3): BFS works in any shape.
- Large snake-shaped island: the iterative flood fill avoids recursion depth issues.

## Variations

- 0-1 BFS with a deque gives the same result without the separate flood fill: land costs 0 (push front), water costs 1 (push back).
- Bidirectional BFS from both islands at once halves the explored area in practice.

See [C2 · Where memory goes in Java solutions](../academy/lessons/C2.md) for why recursion depth matters on grids.

Practise it in the app: Run / Submit on this page.
