**Short answer:** Split it into two problems. First, a multi-source BFS from every cat (thief) gives each cell its distance to the nearest cat. Then find the path from start to end whose *smallest* cell value is as large as possible: a "widest path" search, which is Dijkstra with a max-heap and `min` instead of `+`. Total O(n² log n).

## Approach

- **Brute force:** enumerate paths and take the best minimum. Exponential. Even computing nearest-cat distance per cell by scanning all cats is O(n⁴) on a 400×400 grid.
- **Insight 1: distances once.** Push every cat cell into one BFS queue with distance 0. BFS on an open grid with 4-way moves gives exactly the Manhattan distance to the nearest cat, in O(n²).
- **Insight 2: maximise the bottleneck.** A path's safeness is the minimum `dist` along it. Two standard ways:
  - **Binary search on the answer `k`:** can you go from start to end using only cells with `dist >= k`? That check is a BFS, and it is monotone in `k`. O(n² log n).
  - **Widest-path Dijkstra:** always expand the frontier cell with the largest safeness so far. A cell's safeness via a neighbour is `min(neighbour's safeness, dist[cell])`. Because `min` never increases along a path, the first time the end cell is popped its value is optimal, just as in Dijkstra.

## Solution

```java
import java.util.*;

class Solution {
    private static final int[] DR = {1, -1, 0, 0};
    private static final int[] DC = {0, 0, 1, -1};

    public int maximumSafenessFactor(List<List<Integer>> grid) {
        int n = grid.size();
        // 1. Multi-source BFS from every thief.
        int[][] dist = new int[n][n];
        for (int[] d : dist) Arrays.fill(d, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (grid.get(r).get(c) == 1) {
                    dist[r][c] = 0;
                    q.add(new int[] {r, c});
                }
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = p[0] + DR[d], c = p[1] + DC[d];
                if (r >= 0 && r < n && c >= 0 && c < n && dist[r][c] == -1) {
                    dist[r][c] = dist[p[0]][p[1]] + 1;
                    q.add(new int[] {r, c});
                }
            }
        }
        // 2. Widest path: expand the safest frontier cell first.
        int[][] best = new int[n][n];
        for (int[] b : best) Arrays.fill(b, -1);
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(b[0], a[0]));
        best[0][0] = dist[0][0];
        pq.add(new int[] {dist[0][0], 0, 0});
        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int s = top[0], r0 = top[1], c0 = top[2];
            if (s < best[r0][c0]) continue;               // stale entry
            if (r0 == n - 1 && c0 == n - 1) return s;
            for (int d = 0; d < 4; d++) {
                int r = r0 + DR[d], c = c0 + DC[d];
                if (r < 0 || r >= n || c < 0 || c >= n) continue;
                int ns = Math.min(s, dist[r][c]);
                if (ns > best[r][c]) {
                    best[r][c] = ns;
                    pq.add(new int[] {ns, r, c});
                }
            }
        }
        return best[n - 1][n - 1];
    }
}
```

## Complexity

- **Time O(n² log n):** BFS is O(n²); the heap holds O(n²) entries, each push/pop O(log n²).
- **Space O(n²):** `dist`, `best` and the heap.

## Edge cases

- Start or end is a cat: their `dist` is 0, so the answer is 0 (Example 1).
- `n = 1`: the single cell must be a cat; answer 0.
- Cells with cats are walkable here, they just have safeness 0.

## Follow-ups

- **Plain S->T reachability first.** In the interview story some cells are water (blocked). Reachability is a single BFS or DFS from S over non-water cells; return whether T is visited. O(cells). In the safest-path version, never step on water in the second search. Ask the interviewer whether "distance to the cat" is straight Manhattan distance or walking distance around water. For walking distance, skip water cells in the multi-source BFS too; for Manhattan distance, let that BFS pass through water.

## Variations

- Path With Maximum Minimum Value (LeetCode 1102) and Swim in Rising Water (778) use the same bottleneck search; union-find over cells sorted by value also works.

Practise it in the app: Run / Submit on this page.
