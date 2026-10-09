**Short answer:** Flood-fill each island and describe its shape in a way that ignores position: record every cell as an offset from the island's first cell (the one found first in row-major order). Two islands are translations of each other exactly when their offset sets are equal. Put a canonical form of each set (sorted offsets) into a `HashSet`; its size is the answer. O(m · n · log) or O(m · n) with a traversal-path signature.

## Approach

- **Brute force:** collect each island's cells, then compare every pair of islands by trying to slide one onto the other. O(I² · cells).
- **Key insight:** translation is removed by subtracting a fixed reference cell. Every island has a natural reference: its first cell in row-major scan order, which is always the cell where the flood fill starts. After that, equal shapes give equal offset sets.
- **Canonical form:** the set of offsets must be compared as a set, so sort it before turning it into a key. (The traversal order can vary between two identical shapes only if the code is order-dependent; sorting removes that worry.)
- **Path-signature alternative:** with a recursive DFS in a fixed direction order, record the direction of each step *and* a "back" marker when returning. The string is the same for identical shapes. Without the back markers, different shapes can collide.

## Solution

```java
import java.util.*;

class Solution {
    public int numDistinctIslands(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        boolean[][] seen = new boolean[m][n];
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        Set<String> shapes = new HashSet<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != 1 || seen[i][j]) continue;
                List<Long> cells = new ArrayList<>();          // offsets from (i, j)
                ArrayDeque<int[]> stack = new ArrayDeque<>();
                stack.push(new int[] {i, j});
                seen[i][j] = true;
                while (!stack.isEmpty()) {
                    int[] c = stack.pop();
                    cells.add((long) (c[0] - i) * 1000 + (c[1] - j));
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], k = c[1] + dc[d];
                        if (r >= 0 && r < m && k >= 0 && k < n && grid[r][k] == 1 && !seen[r][k]) {
                            seen[r][k] = true;
                            stack.push(new int[] {r, k});
                        }
                    }
                }
                Collections.sort(cells);                       // canonical order
                shapes.add(cells.toString());
            }
        }
        return shapes.size();
    }
}
```

The offset encoding `dr * 1000 + dc` is unique because column offsets are within ±50 here; with larger grids use a wider multiplier or a pair of ints.

## Complexity

- **Time:** O(m · n · log(m · n)) in the worst case because of sorting each island's cells; the flood fill itself is O(m · n).
- **Space:** O(m · n) for `seen`, the stack and the stored shape keys.

## Edge cases

- No land: 0.
- Mirror images (Example 2) are different shapes: no normalisation for reflection.
- Single-cell islands all share one shape.
- Column offsets can be negative (the first cell is not always the leftmost), so the encoding must handle negatives; `dr * 1000 + dc` with |dc| < 1000 does.

## Variations

- **Number of Distinct Islands II (rotations and reflections count as the same):** generate all 8 transforms of the cell list, normalise each (translate so the minimum row and column are 0, then sort), and use the lexicographically smallest as the key.
- **Distinct islands in a binary tree:** serialise each island's subtree shape the same way.
- **Count islands of each shape:** use a `HashMap<String, Integer>` instead of a set.

Practise it in the app: Run / Submit on this page.
