**Short answer:** The nodes reachable from `x` are exactly the other nodes in `x`'s connected component, so the answer is `componentSize(x) − 1`. Compute every component's size once, then answer each query in O(1). Two standard ways: label components with DFS/BFS, or union all edges in a disjoint-set union (DSU) that tracks sizes.

## Approach

**Brute force.** Run a fresh DFS from every query node. That is O(q · (n + E)), too slow when most nodes sit in one big component and there are many queries.

**Key insight.** Reachability in an undirected graph is the same for every node in a component. Precompute once, answer many times.

**Option A: DFS/BFS labelling.** Build adjacency lists. For each unvisited node, BFS its component, give every node in it the same component id, and record the size. Answer = `size[comp[q]] − 1`.

**Option B: DSU.** Start with each node alone. For every edge, `union(u, v)`. Keep the size at each root. Answer = `size[find(q)] − 1`. No adjacency lists needed.

The reference uses DSU.

## Solution

```java
import java.util.*;

class Solution {
    private int[] parent, size;

    public int[] countReachable(int n, int[][] edges, int[] queries) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        for (int[] e : edges) union(e[0], e[1]);
        int[] ans = new int[queries.length];
        for (int i = 0; i < queries.length; i++) ans[i] = size[find(queries[i])] - 1;
        return ans;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];   // path halving
            x = parent[x];
        }
        return x;
    }

    private void union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a == b) return;                  // duplicate edge or self-loop
        if (size[a] < size[b]) { int t = a; a = b; b = t; }
        parent[b] = a;                       // union by size
        size[a] += size[b];
    }
}
```

The DFS version for comparison (iterative, so a long chain cannot overflow the stack):

```java
int[] comp = new int[n];
Arrays.fill(comp, -1);
List<Integer> sizes = new ArrayList<>();
for (int s = 0; s < n; s++) {
    if (comp[s] != -1) continue;
    int id = sizes.size(), count = 0;
    Deque<Integer> stack = new ArrayDeque<>(List.of(s));
    comp[s] = id;
    while (!stack.isEmpty()) {
        int u = stack.pop();
        count++;
        for (int v : adj.get(u)) if (comp[v] == -1) { comp[v] = id; stack.push(v); }
    }
    sizes.add(count);
}
// answer for q: sizes.get(comp[q]) - 1
```

## Complexity

- **DSU:** O((n + E) · α(n)) to build, O(α(n)) per query, which is effectively constant. Space O(n).
- **DFS labelling:** O(n + E) to build, O(1) per query. Space O(n + E) for the adjacency lists.

## Edge cases

- No edges: every answer is 0.
- Self-loops and duplicate edges: `find(a) == find(b)`, so `union` does nothing and sizes stay right.
- Repeated queries: just look up the same size again.
- An isolated queried node: 0, since a node never counts itself.

## Follow-ups

- **When is DSU more suitable?** When edges arrive over time (online connectivity: "add edge, then ask"), because DSU updates in near-constant time while DFS would have to recompute. Also when you only need connectivity, not paths, and do not want to build adjacency lists. DFS/BFS is better when you need the actual path, distances, or when edges are deleted (DSU cannot split sets).
- **Code clarity and reusability.** Pull the DSU into its own small class (`find`, `union`, `size`) with union by size and path compression, so it is reused across problems and tested once. Keep the problem method short: build, then answer. Name the answer clearly as "component size minus one".

See [C5 · Amortised analysis](../academy/lessons/C5.md) for why DSU operations are near-constant on average.

Practise it in the app: Run / Submit on this page.
