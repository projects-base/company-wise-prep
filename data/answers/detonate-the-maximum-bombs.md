**Short answer:** Turn the bombs into a **directed** graph: edge i → j when bomb j's centre is within bomb i's radius (`dx² + dy² ≤ rᵢ²`, compared in `long`). Then the answer is the largest set of nodes reachable from a single start, so BFS from every bomb and keep the biggest count. With n ≤ 100 that is O(n³) worst case, which is tiny. Union-Find does not work here because reach is one-way.

## Approach

**Simulation by hand** is exactly what we do, but it needs the right structure.

**Key insight 1 — it is a graph.** "Bomb i sets off bomb j" depends only on i's radius and the distance between centres. Precompute it once as adjacency lists.

**Key insight 2 — edges are directed.** A big bomb can reach a small one that cannot reach back (Example 1). So connected components and Union-Find give wrong answers: they would merge bombs that cannot trigger each other in both directions.

**Optimal for these limits.** For each start s, BFS (or DFS) and count visited nodes. Keep the maximum.

## Solution

```java
import java.util.*;

class Solution {
    // Directed graph i -> j when j's centre is inside i's circle; BFS from every bomb.
    public int maximumDetonation(int[][] bombs) {
        int n = bombs.length;
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            long r = bombs[i][2];
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                long dx = bombs[i][0] - bombs[j][0], dy = bombs[i][1] - bombs[j][1];
                if (dx * dx + dy * dy <= r * r) adj.get(i).add(j);
            }
        }
        int best = 0;
        for (int s = 0; s < n; s++) {
            boolean[] seen = new boolean[n];
            ArrayDeque<Integer> q = new ArrayDeque<>();
            seen[s] = true;
            q.add(s);
            int count = 0;
            while (!q.isEmpty()) {
                int u = q.poll();
                count++;
                for (int v : adj.get(u)) {
                    if (!seen[v]) {
                        seen[v] = true;
                        q.add(v);
                    }
                }
            }
            best = Math.max(best, count);
        }
        return best;
    }
}
```

Compare squared distances, never `Math.sqrt`, to avoid floating-point error on boundary cases ("within or on" means `≤`). The coordinate difference fits in `int`, but its square (up to 2·10¹⁰ for the sum) does not, so `dx`, `dy` and `r` are `long` before multiplying.

## Complexity

- **Time:** O(n²) to build the graph, plus n BFS runs of O(n + E) each with E ≤ n². Worst case O(n³) = 10⁶ for n = 100.
- **Space:** O(n²) for the adjacency lists.

## Edge cases

- One bomb → 1.
- Bombs at the same point: distance 0, so they reach each other.
- Exactly on the boundary: counts (`≤`).
- Early exit: if a BFS reaches all n bombs, return n immediately.

## Variations

- **Large n:** compute strongly connected components (Tarjan/Kosaraju), condense to a DAG, and find the source component with the largest reachable set. Exact reachable-set sizes in a DAG are still hard in general (bitsets help: O(n²/64)), but the condensation removes a lot of repeated work.
- **Find the minimum number of bombs to detonate everything:** count the source SCCs of the condensed DAG.

## Follow-ups

- The YAML only says "Follow-up was solved in the same round", without saying what it was. Likely candidates are the two variations above (scaling to large n via SCCs, or the minimum number of manual detonations to clear all bombs).

See also [C1 · From constraints to the expected complexity](../academy/lessons/C1.md).

Practise it in the app: Run / Submit on this page.
