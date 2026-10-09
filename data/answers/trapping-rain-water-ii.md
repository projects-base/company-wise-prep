**Short answer:** Water escapes through the lowest point of the wall around it, so flood the grid inwards from the border with a min-heap. Push every border cell, then repeatedly pop the lowest boundary cell and visit its unvisited neighbours. A neighbour lower than the popped level traps `level - height` water. Push it back with level `max(level, height)`. This runs in O(mn log(mn)).

## Approach

- **Why the 1D trick fails.** In 2D, water can leak out in any direction, not just left or right. The water level of a cell is the lowest "highest wall" on any path from that cell to the border. Row and column maxima do not capture that.
- **Brute force.** Repeatedly lower each cell's water level to its neighbours' levels until nothing changes (Bellman-Ford style). It needs many full-grid passes, which is too slow for 200×200.
- **Key insight.** Treat the border as the current wall. Its lowest cell decides how high water can stand just inside it. Pop the lowest wall cell. Any unvisited neighbour can hold water up to that level, and no lower exit exists, because every other wall cell is at least as high. The neighbour then joins the wall at height `max(level, height)`. This is Dijkstra with a minimax cost, the same idea as Swim in Rising Water.

## Solution

```java
import java.util.*;

class Solution {
    public int trapRainWater(int[][] heightMap) {
        int m = heightMap.length, n = heightMap[0].length;
        if (m < 3 || n < 3) return 0;
        boolean[][] seen = new boolean[m][n];
        // min-heap of {level, row, col}; start from the whole border
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (i == 0 || j == 0 || i == m - 1 || j == n - 1) {
                    seen[i][j] = true;
                    pq.add(new int[] {heightMap[i][j], i, j});
                }
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        int water = 0;
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            for (int d = 0; d < 4; d++) {
                int r = cur[1] + dr[d], c = cur[2] + dc[d];
                if (r < 0 || c < 0 || r >= m || c >= n || seen[r][c]) continue;
                seen[r][c] = true;
                water += Math.max(0, cur[0] - heightMap[r][c]);
                pq.add(new int[] {Math.max(cur[0], heightMap[r][c]), r, c});
            }
        }
        return water;
    }
}
```

## Complexity

- **Time:** O(mn log(mn)). Each cell is pushed and popped once.
- **Space:** O(mn) for the heap and the `seen` array.

## Edge cases

- Fewer than 3 rows or columns: every cell is on the border, so the answer is 0.
- A bowl with a single low gap in its rim: the water drains to the gap's height, and the heap order finds that.
- Flat grid: 0.
- Interior cells taller than the rim: they hold no water but act as walls for the cells inside them.

## Variations

- **1D Trapping Rain Water:** two pointers, O(1) space.
- **Swim in Rising Water / Path With Minimum Effort:** the same minimax Dijkstra.
- **Water level per cell:** store `max(level, height)` at visit time instead of only summing.

Practise it in the app: Run / Submit on this page.
