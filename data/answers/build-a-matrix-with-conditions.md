**Short answer:** Row conditions and column conditions are independent. Each set is a directed graph on 1..k, and any topological order of it gives every number a valid row (or column) index. Run Kahn's algorithm twice; if either graph has a cycle, return the empty matrix. Otherwise place number `v` at `(rowIndex[v], colIndex[v])`. Every number has its own row and its own column, so nothing collides.

## Approach

**Brute force.** Trying placements cell by cell with backtracking is exponential.

**Key insight.** "`a` above `b`" constrains only row indices, and "`a` left of `b`" constrains only column indices. The matrix is k × k and there are k numbers, so each number can get a distinct row and a distinct column. The row problem is then: order 1..k so every edge `a → b` has `a` before `b`. That is a topological sort. The same holds for columns.

A cycle in either graph means the conditions contradict each other, so no matrix exists.

**Optimal.** Two Kahn's-algorithm passes and an O(k²) fill.

## Solution

```java
import java.util.*;

class Solution {
    public int[][] buildMatrix(int k, int[][] rowConditions, int[][] colConditions) {
        int[] rowOrder = topoOrder(k, rowConditions);
        int[] colOrder = topoOrder(k, colConditions);
        if (rowOrder == null || colOrder == null) return new int[0][];
        int[] rowOf = new int[k + 1], colOf = new int[k + 1];
        for (int i = 0; i < k; i++) {
            rowOf[rowOrder[i]] = i;
            colOf[colOrder[i]] = i;
        }
        int[][] m = new int[k][k];
        for (int v = 1; v <= k; v++) m[rowOf[v]][colOf[v]] = v;
        return m;
    }

    /** Kahn's algorithm over 1..k; null if the conditions contain a cycle. */
    @SuppressWarnings("unchecked")
    private int[] topoOrder(int k, int[][] conds) {
        List<Integer>[] out = new List[k + 1];
        for (int v = 1; v <= k; v++) out[v] = new ArrayList<>();
        int[] indeg = new int[k + 1];
        for (int[] c : conds) {
            out[c[0]].add(c[1]);
            indeg[c[1]]++;
        }
        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 1; v <= k; v++) if (indeg[v] == 0) q.add(v);
        int[] order = new int[k];
        int n = 0;
        while (!q.isEmpty()) {
            int v = q.poll();
            order[n++] = v;
            for (int w : out[v]) if (--indeg[w] == 0) q.add(w);
        }
        return n == k ? order : null;
    }
}
```

## Complexity

- **Time:** O(k + R + C) for the two sorts (R and C are the condition counts), plus O(k²) to create and fill the matrix, which dominates for large k.
- **Space:** O(k + R + C) for the graphs, plus O(k²) for the output.

## Edge cases

- Repeated conditions: each adds an edge and one in-degree; both are removed the same number of times, so Kahn's still works.
- Numbers with no conditions start with in-degree 0 and still get a row and a column.
- A cycle in only the column graph still means "return the empty matrix".
- Cycle detection is by counting: fewer than k numbers popped means a cycle.

## Variations

- Use DFS with three states (unvisited, in progress, done) instead of Kahn's; reaching an "in progress" node is a cycle.
- Course Schedule II is the same topological sort on a single graph.

Practise it in the app: Run / Submit on this page.
