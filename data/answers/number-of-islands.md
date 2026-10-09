**Short answer:** Treat the grid as a graph where each land cell is a node with edges to its land neighbours (up, down, left, right). The number of islands is the number of connected components. Scan every cell; when you find unvisited land, count one island and flood-fill it with BFS or DFS so you never count it again. O(m · n) time.

## Approach

- **Key insight:** "connected land" is exactly a connected component. Each flood fill removes one whole component, so the number of fills started is the answer.
- **Visited marking:** either a `boolean[][]` or, if modifying the input is allowed, overwrite land with `'0'`. Mark a cell when you *enqueue* it, not when you dequeue it, or it can be enqueued several times.
- **BFS vs DFS:** both are O(m · n). Recursive DFS can hit 90,000 levels on a 300 × 300 snake and throw `StackOverflowError`; use BFS or an explicit stack.
- **Union-find** also works: union each land cell with its right and down land neighbours, then count roots. Useful when land is added over time (Number of Islands II).

## Solution

BFS:

```java
import java.util.ArrayDeque;

class Solution {
    public int numIslands(char[][] grid) {
        int m = grid.length, n = grid[0].length, count = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != '1') continue;
                count++;
                grid[i][j] = '0';                      // mark on enqueue
                q.add(new int[] {i, j});
                while (!q.isEmpty()) {
                    int[] c = q.poll();
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == '1') {
                            grid[r][k] = '0';
                            q.add(new int[] {r, k});
                        }
                    }
                }
            }
        }
        return count;
    }
}
```

DFS (recursive; fine for small grids, risky for long islands):

```java
class Solution {
    public int numIslands(char[][] grid) {
        int count = 0;
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[0].length; j++)
                if (grid[i][j] == '1') { count++; sink(grid, i, j); }
        return count;
    }

    private void sink(char[][] g, int r, int c) {
        if (r < 0 || r >= g.length || c < 0 || c >= g[0].length || g[r][c] != '1') return;
        g[r][c] = '0';
        sink(g, r + 1, c); sink(g, r - 1, c); sink(g, r, c + 1); sink(g, r, c - 1);
    }
}
```

## Complexity

- **Time:** O(m · n): each cell is visited a constant number of times.
- **Space:** O(min(m, n)) for the BFS queue in typical shapes, O(m · n) worst case; DFS uses O(m · n) stack in the worst case.

## Edge cases

- All water: 0. All land: 1.
- Diagonal neighbours do not connect (Example 1 has 3 islands).
- One row or one column.
- The grid holds `char`s, so compare with `'1'`, not `1`.
- Mutating the input: ask first; otherwise use a `seen` array.

## Follow-ups

- **Why use a graph for a matrix problem?** Because the question is about connectivity, and connectivity is a graph question. Cells are nodes, adjacency defines edges. Once you see that, standard tools apply (BFS, DFS, union-find), and so do their guarantees and complexities. The matrix is just a compact way to store a grid graph without building an adjacency list.
- **BFS and DFS:** both shown above. BFS needs a queue and never recurses; DFS is shorter to write but can overflow the stack on long islands (use an explicit stack to make it safe).
- **The matrix does not fit in one machine's memory:**
  - *Stream by rows:* you only need the previous row's component labels. Process one row at a time with union-find over labels: a new land cell takes the label of its left or upper neighbour, and two labels are unioned when both neighbours are land. Count a component as finished when none of its labels appear in the current row. Memory is O(n) per row plus the live labels.
  - *Distributed:* split the grid into tiles. Each worker counts islands in its tile and exports the labels of land cells on its borders. A merge step unions labels that touch across tile borders (union-find), and the answer is the sum of tile counts minus the number of successful cross-border unions. This is the map-reduce version of connected-component labelling.

Practise it in the app: Run / Submit on this page.
