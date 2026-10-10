**Short answer:** Partition DP. Let `dp[day][i]` be the minimum difficulty to finish the first i jobs in `day` days. The last day takes some contiguous block `j..i−1`, so `dp[day][i] = min over j of dp[day−1][j] + max(job[j..i−1])`. Iterate j downward from i − 1 so the block maximum is updated in O(1). O(d · n²) = 10 × 300² ≈ 10⁶. Return −1 if there are fewer jobs than days.

## Picture it

Example 1: `jobDifficulty = [6,5,4,3,2,1]`, `d = 2`. Row = days used, column i = first i jobs done (`∞` = impossible).

| days \ i | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|---|
| 0 | 0 | ∞ | ∞ | ∞ | ∞ | ∞ | ∞ |
| 1 | ∞ | 6 | 6 | 6 | 6 | 6 | 6 |
| 2 | ∞ | ∞ | 11 | 10 | 9 | 8 | **7** |

Each cell `dp[day][i]` = min over j of `dp[day − 1][j] + max(jobs j..i−1)`: it reads the row above at every column j < i, with the last day's max grown as a running max while j walks left.

How `dp[2][6]` is chosen (j walks from 5 down to 1):

| j | Last day's jobs | Running max | dp[1][j] + max |
|---|---|---|---|
| 5 | [1] | 1 | 6 + 1 = **7** |
| 4 | [2,1] | 2 | 6 + 2 = 8 |
| 3 | [3,2,1] | 3 | 9 |
| 2 | [4,3,2,1] | 4 | 10 |
| 1 | [5,4,3,2,1] | 5 | 11 |

**The picture in one sentence:** split at the last cut, so each cell is the previous day's best for the prefix plus the running max of the final block.

## Approach

- **Brute force:** try every way to place d − 1 cut points among n − 1 gaps: C(n−1, d−1), which explodes for n = 300.
- **Greedy fails:** putting the hardest job alone on a day is not always best; it depends on what else shares the day.
- **Key insight:** order is fixed and each day is a contiguous block, so the problem splits at the last cut. What happens before the last cut only matters through its best total, which is exactly `dp[day − 1][j]`.
- **Running max:** walking j from right to left extends the last block one job at a time, so its max is a running max. No range-max structure needed.
- **Rolling arrays:** each day only uses the previous day's row.

## Solution

```java
import java.util.Arrays;

class Solution {
    public int minDifficulty(int[] jobDifficulty, int d) {
        int n = jobDifficulty.length;
        if (n < d) return -1;
        final int INF = Integer.MAX_VALUE / 2;
        int[] dp = new int[n + 1];          // dp[i]: best for the first i jobs with the days so far
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int day = 1; day <= d; day++) {
            int[] next = new int[n + 1];
            Arrays.fill(next, INF);
            for (int i = day; i <= n; i++) {            // need at least one job per day
                int mx = 0;
                for (int j = i - 1; j >= day - 1; j--) { // last day covers jobs j..i-1
                    mx = Math.max(mx, jobDifficulty[j]);
                    if (dp[j] < INF) next[i] = Math.min(next[i], dp[j] + mx);
                }
            }
            dp = next;
        }
        return dp[n];
    }
}
```

## Complexity

- **Time:** O(d · n²) ≈ 9·10⁵ operations.
- **Space:** O(n) with two rolling rows.

## Edge cases

- `n < d`: impossible, −1.
- `n == d`: one job per day, the answer is the sum of all difficulties.
- `d == 1`: the maximum of all jobs.
- Difficulty 0 jobs are allowed; `mx` starts at 0, which is correct because each block has at least one job.
- Loop bounds: `j ≥ day − 1` keeps at least one job for each earlier day.

## Variations

- **O(d · n) with a monotonic stack:** for a fixed day, the "last block max" is a classic "previous greater element" structure. Keep a stack of (max, bestBefore) and merge blocks whose max is dominated. Worth mentioning; the O(d · n²) DP is the expected answer at these limits.
- **Split Array Largest Sum:** minimise the *maximum* block sum instead of the sum of block maxima; binary search on the answer works there.
- **Return the schedule:** store the best j for each (day, i) and walk back.

See [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
