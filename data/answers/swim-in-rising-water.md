**Short answer:** The answer is the smallest possible "highest cell" over all paths from the top-left to the bottom-right corner (a minimax path). Run Dijkstra where a path's cost is the maximum elevation on it instead of the sum: pop the cell with the lowest cost, relax neighbours with `max(cost, grid[neighbour])`. The first time the target is popped, its cost is the answer. O(n² log n).

## Approach

- **Brute force.** For each `t` from 0 upwards, BFS over cells with elevation ≤ t and stop at the first `t` that connects the corners. Up to n² values of t, each O(n²): O(n⁴), 6.25 million cell visits for n = 50. Works, but wasteful.
- **Binary search on t + BFS.** Reachability is monotonic in t, so binary search: O(n² log n²). A solid answer.
- **Key insight (Dijkstra on max).** Dijkstra works for any path cost that never decreases when the path is extended. `max` qualifies. So the classic algorithm finds the path with the smallest maximum directly.
- **Union-find alternative.** Add cells in increasing elevation order, union each with already-added neighbours, and stop when the two corners are connected. The elevation of the cell just added is the answer. Values are a permutation of `0..n²-1`, so you can index cells by elevation without sorting.

## Solution

```java
import java.util.*;

class Solution {
    public int swimInWater(int[][] grid) {
        int n = grid.length;
        // Dijkstra where a path's cost is the highest elevation on it.
        int[][] best = new int[n][n];
        for (int[] row : best) Arrays.fill(row, Integer.MAX_VALUE);
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        best[0][0] = grid[0][0];
        pq.add(new int[] {grid[0][0], 0, 0});
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int t = cur[0], r = cur[1], c = cur[2];
            if (t > best[r][c]) continue;              // stale entry
            if (r == n - 1 && c == n - 1) return t;
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr < 0 || nr >= n || nc < 0 || nc >= n) continue;
                int nt = Math.max(t, grid[nr][nc]);
                if (nt < best[nr][nc]) {
                    best[nr][nc] = nt;
                    pq.add(new int[] {nt, nr, nc});
                }
            }
        }
        return -1; // unreachable: the grid is connected
    }
}
```

Note the start cost is `grid[0][0]`, not 0: you must wait for the water to cover the start cell too.

## Complexity

- **Time:** O(n² log n²) = O(n² log n). Each cell is pushed at most four times, each push or pop is O(log n²).
- **Space:** O(n²) for `best` and the heap.

## Edge cases

- n = 1: the answer is `grid[0][0]`.
- The target is the highest cell: the answer is its elevation.
- The start is the highest cell: the answer is `grid[0][0]`.

## Variations

- **Path With Minimum Effort (LC 1631):** cost is the max absolute height difference along the path; same Dijkstra-on-max.
- **Minimum spanning tree view:** the minimax path between two nodes lies on the MST, which is why the union-find (Kruskal) approach works.
- **Trapping Rain Water II:** a related "water level spreading from a boundary" heap algorithm.

Practise it in the app: Run / Submit on this page.
