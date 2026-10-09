**Short answer:** Every move goes one column to the right, so a path visits exactly one cell in each column. That turns the problem into a column-by-column DP: `ways[c][r] = ways[c−1][r−1] + ways[c−1][r] + ways[c−1][r+1]`. A checkpoint `(r, c)` simply forces the path to use row `r` in column `c`, so after computing column `c` you zero every other row. Two different checkpoints in the same column make the answer 0. Keep one column at a time for O(n) space.

## Approach

- **Brute force:** DFS every path from the start and check checkpoints at the end. Up to 3^m paths. Hopeless for `m = 1000`.
- **Plain counting:** DP over columns. Paths into `(r, c)` come from `(r−1, c−1)`, `(r, c−1)` or `(r+1, c−1)`. Start with `dp[n−1] = 1` in column 0 and read `dp[n−1]` in the last column. O(n·m).
- **Key insight for checkpoints:** since a path has exactly one cell per column, "visits `(r, c)`" is the same as "its cell in column `c` is row `r`". So a checkpoint is a filter on one column. No need to split the path into segments or try orders. Multiple checkpoints in different columns are independent filters; the order of visiting is forced by column order.

## Solution

```java
import java.util.*;

class Solution {
    private static final int MOD = 1_000_000_007;

    public int countPaths(int n, int m, int[][] checkpoints) {
        int[] forced = new int[m];
        Arrays.fill(forced, -1);
        for (int[] cp : checkpoints) {
            if (forced[cp[1]] != -1 && forced[cp[1]] != cp[0]) return 0; // two rows in one column
            forced[cp[1]] = cp[0];
        }
        long[] dp = new long[n];
        dp[n - 1] = 1;
        applyForced(dp, forced[0]);
        for (int c = 1; c < m; c++) {
            long[] next = new long[n];
            for (int r = 0; r < n; r++) {
                long ways = dp[r];
                if (r > 0) ways += dp[r - 1];
                if (r + 1 < n) ways += dp[r + 1];
                next[r] = ways % MOD;
            }
            dp = next;
            applyForced(dp, forced[c]);
        }
        return (int) dp[n - 1];
    }

    private static void applyForced(long[] dp, int row) {
        if (row < 0) return;
        for (int r = 0; r < dp.length; r++) if (r != row) dp[r] = 0;
    }
}
```

Each term is below `MOD`, so a sum of three fits easily in a `long` before the `%`.

## Complexity

- **Time O(n·m + k)** for `k` checkpoints.
- **Space O(n + m):** two rows of `n` counts plus the `forced` array of size `m`.

## Edge cases

- A checkpoint in column 0 not on row `n−1`, or in the last column not on row `n−1`: answer 0 (handled by the forcing).
- Duplicate checkpoints: same row, no conflict.
- `n = 1`: only the straight path; answer 1 unless a checkpoint conflicts (it cannot, row is 0).
- `m = 1`: start equals end, answer 1.

## Follow-ups

- **Count only paths that visit all checkpoints.** Exactly the forcing above: zero all other rows in a checkpoint's column.
- **Checkpoints must be visited in a given order; optimise space.** Because columns only increase, the visiting order is the order of their columns. If the given order is not non-decreasing by column (or two consecutive ones share a column with different rows), the answer is 0. Otherwise it is the same DP. Space is already O(n) with a rolling row; you could even update in place with one temp variable for the previous `dp[r−1]`. In the general (non-forced) case you would multiply segment counts between consecutive checkpoints, but the one-cell-per-column property makes that unnecessary here.

Practise it in the app: Run / Submit on this page.
