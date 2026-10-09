**Short answer:** The value of a cell is decided by the **last** query that covers it. So process the queries backwards and write each cell only once. A union-find "next unwritten index" pointer lets you skip cells that are already written, giving near O(n + q) total. A segment tree with lazy assignment is the other standard answer, at O((n + q) log n).

## Approach

- **Brute force.** Apply each query cell by cell. Worst case 10⁵ queries × 10⁵ cells = 10¹⁰ writes. Too slow.
- **Key insight.** Later queries overwrite earlier ones completely. Reading queries in reverse, the first query to touch a cell is the final value, and after that the cell is frozen. Every cell is written at most once, so the total work is O(n) writes, as long as you can jump over frozen cells quickly.
- **Skipping frozen cells.** `next[i]` points to the smallest index `≥ i` that is still unwritten. When cell `i` is written, set `next[i] = i + 1`. `find(i)` follows pointers with path compression, so long runs of written cells are skipped in amortised near-constant time. Index `n` is a sentinel.
- **Alternative: lazy segment tree.** Each node stores a pending "assign k" tag. An update on `[l, r]` tags O(log n) nodes and pushes tags down when partially covering a node. At the end, push all tags to the leaves. Use this when queries are interleaved with reads (online), which the backwards trick cannot handle.

## Solution

```java
class Solution {
    private int[] next; // next[i]: smallest index >= i not yet written (n means none)

    public int[] applyAssignments(int[] arr, int[][] queries) {
        int n = arr.length;
        int[] out = arr.clone();
        next = new int[n + 1];
        for (int i = 0; i <= n; i++) next[i] = i;
        // The last query covering a cell decides its value, so go backwards.
        for (int q = queries.length - 1; q >= 0; q--) {
            int l = queries[q][0], r = queries[q][1], k = queries[q][2];
            for (int i = find(l); i <= r; i = find(i)) {
                out[i] = k;
                next[i] = i + 1;
            }
        }
        return out;
    }

    private int find(int x) { // iterative, avoids deep recursion on 1e5 cells
        int root = x;
        while (next[root] != root) root = next[root];
        while (next[x] != root) {
            int t = next[x];
            next[x] = root;
            x = t;
        }
        return root;
    }
}
```

## Complexity

- **Time:** O((n + q) · α(n)) amortised. Each cell is written once, and each query does one extra `find` that lands past `r`.
- **Space:** O(n) for `next` and the output copy.

## Edge cases

- No queries: return the original array.
- Single-cell queries (`l == r`).
- A query fully inside a region already written: the first `find(l)` jumps past `r`, so it costs O(α) only.
- Recursion: a recursive `find` can overflow the stack on a long chain; the iterative version avoids it.

## Follow-ups

- **Same `k` for every query vs different `k`.** If every query sets the same value, order does not matter. Sort the intervals, merge overlapping ones, and fill the union, or use a difference array (`+1` at `l`, `-1` at `r + 1`, prefix sum, cell covered if the sum is positive). That is O(n + q log q) or O(n + q). With different `k`, order matters, which is why you need the backwards pass or the lazy segment tree.
- **Online reads between updates:** use the lazy segment tree, or an ordered map of disjoint constant runs (`TreeMap<start, value>`, a "Chtholly tree"), where each assignment splits at `l` and `r + 1` and replaces everything in between.

Practise it in the app: Run / Submit on this page.
