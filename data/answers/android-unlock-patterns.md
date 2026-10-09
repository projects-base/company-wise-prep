**Short answer:** Backtrack from each starting dot, counting every path whose length is between `m` and `n`. A `skip[a][b]` table stores the dot exactly between `a` and `b` (or 0). A move is allowed only if the target is unused and its middle dot is 0 or already used. By symmetry, start once from a corner (×4), once from an edge midpoint (×4) and once from the centre.

## Approach

- **Brute force:** generate every ordered sequence of distinct dots up to length 9 (at most 9! ≈ 363k full sequences, about 986k prefixes) and check each one. This is already small, so plain backtracking is the intended solution. The work is in getting the jump rule right.
- **Key insight 1 (the rule as data):** only 8 unordered pairs have a dot in the middle: the three rows (1-3, 4-6, 7-9), the three columns (1-7, 2-8, 3-9) and the two diagonals (1-9, 3-7). Put them in a table instead of using geometry. Moves like 1 → 6 or 2 → 9 pass through no dot centre, so they are always allowed.
- **Key insight 2 (symmetry):** the grid looks the same under rotation and reflection. All four corners give the same count, and so do all four edge midpoints. So run 3 DFS calls instead of 9.
- **Counting:** each recursive call stands for a path of length `len`. It counts itself if `len >= m`, then extends unless `len == n`.

## Solution

```java
class Solution {
    public int numberOfPatterns(int m, int n) {
        // skip[a][b] = the dot exactly between a and b, or 0 if none
        int[][] skip = new int[10][10];
        skip[1][3] = skip[3][1] = 2;
        skip[4][6] = skip[6][4] = 5;
        skip[7][9] = skip[9][7] = 8;
        skip[1][7] = skip[7][1] = 4;
        skip[2][8] = skip[8][2] = 5;
        skip[3][9] = skip[9][3] = 6;
        skip[1][9] = skip[9][1] = 5;
        skip[3][7] = skip[7][3] = 5;
        boolean[] used = new boolean[10];
        int total = 0;
        total += 4 * dfs(1, 1, m, n, skip, used);   // corners 1, 3, 7, 9
        total += 4 * dfs(2, 1, m, n, skip, used);   // edges 2, 4, 6, 8
        total += dfs(5, 1, m, n, skip, used);       // centre
        return total;
    }

    private int dfs(int cur, int len, int m, int n, int[][] skip, boolean[] used) {
        int count = len >= m ? 1 : 0;
        if (len == n) return count;
        used[cur] = true;
        for (int next = 1; next <= 9; next++) {
            if (used[next]) continue;
            int mid = skip[cur][next];
            if (mid != 0 && !used[mid]) continue;   // jumping over an unused dot
            count += dfs(next, len + 1, m, n, skip, used);
        }
        used[cur] = false;                           // backtrack
        return count;
    }
}
```

## Complexity

- **Time:** O(9!) in the worst case — bounded by the number of partial patterns, under a million. It is constant for a fixed 3×3 grid.
- **Space:** O(9) for the recursion depth and the `used` array.

## Edge cases

- `m = n = 1` → 9.
- `m = 1, n = 2` → 65. This checks the table: 72 ordered pairs minus 16 blocked jumps.
- `m = n = 9` → only full patterns are counted.
- Forgetting the reverse direction in the table (`skip[3][1]`) is the most common bug.

## Variations

- **Bitmask memoisation:** state `(cur, usedMask)` gives at most 9 × 512 states, so the count can be memoised. That matters for larger grids, but the jump rule gets harder to precompute there.
- **Validate one given pattern:** walk it once and apply the same `used`/`skip` checks.

Practise it in the app: Run / Submit on this page.
