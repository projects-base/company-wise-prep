**Short answer:** Scan the grid. Each time you meet an unvisited land cell, flood-fill its island (DFS or BFS), marking cells visited and counting them. The largest count is the answer. Every cell is visited once, so it is O(m · n). Use an explicit stack, because one long snake-shaped island can make recursion 90,000 calls deep.

## Approach

- **Brute force:** from every land cell, run a fresh search and count its island. O((m·n)²), because the same island is searched once per cell.
- **Key insight:** mark cells as visited the first time you see them. Then each island is explored exactly once, from its first cell in scan order.
- **Iterative flood fill:** push a cell, mark it seen *when pushing* (not when popping), so a cell is never pushed twice.

## Solution

```java
import java.util.ArrayDeque;

class Solution {
    public int maxAreaOfIsland(int[][] grid) {
        int m = grid.length, n = grid[0].length, best = 0;
        boolean[][] seen = new boolean[m][n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != 1 || seen[i][j]) continue;
                seen[i][j] = true;
                stack.push(new int[] {i, j});
                int area = 0;
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    area++;
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1 && !seen[r][k]) {
                            seen[r][k] = true;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                best = Math.max(best, area);
            }
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(m · n): each cell is pushed and popped at most once, with 4 neighbour checks.
- **Space:** O(m · n) for `seen` and, in the worst case, the stack. You can drop `seen` by setting visited land to 0, if modifying the input is allowed (ask first).

## Edge cases

- No land: 0.
- All land: m · n.
- Diagonal neighbours do not connect.
- Snake-shaped island across a 300 × 300 grid: recursive DFS can throw `StackOverflowError` on the default thread stack; the explicit stack is safe.
- Marking on pop instead of push: a cell can be pushed several times and counted twice. A classic bug.

## Follow-up: you may set one entire row or column to 1s

That is [Largest Connected Component After Filling One Row or Column](max-area-after-filling-row-or-column.md). The step from here: label every island with an id and size in one pass (this code, plus an `id` grid). For each row r, the new component is the C filled cells plus the sizes of all *distinct* islands touching rows r−1, r, r+1, minus the land cells already in row r. Same for columns. That is O(m · n) overall instead of re-running the flood fill per line.

## Variations

- **Number of Islands:** count flood fills instead of measuring them.
- **Making a Large Island (flip one 0):** label islands, then for each 0 sum the distinct neighbouring island sizes + 1.
- **Number of Distinct Islands:** record each island's shape.

Practise it in the app: Run / Submit on this page.
