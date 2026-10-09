**Short answer:** Binary search on the answer. For a candidate cap `C`, a greedy left-to-right pass counts how many pieces you need if no piece may exceed `C`. Fewer pieces are needed as `C` grows, so find the smallest `C` that needs at most `k` pieces. The range is `[max element, total sum]`, so it is O(n log(sum)). The DP over (prefix, pieces) is O(k·n²) and is the stepping stone the interviewer asked for first.

## Approach

**DP first.** Let `dp[j][i]` be the best largest-sum when the first `i` elements are cut into `j` pieces. The last piece is `nums[p..i-1]` for some `p`:

`dp[j][i] = min over p of max(dp[j-1][p], prefix[i] - prefix[p])`, with `dp[0][0] = 0`.

That is O(k·n²) time, 50 × 10⁶ = 5·10⁷ steps here: acceptable, but not elegant.

```java
class SolutionDp {
    public int splitArray(int[] nums, int k) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + nums[i];
        long[][] dp = new long[k + 1][n + 1];
        for (long[] row : dp) java.util.Arrays.fill(row, Long.MAX_VALUE);
        dp[0][0] = 0;
        for (int j = 1; j <= k; j++)
            for (int i = j; i <= n; i++)
                for (int p = j - 1; p < i; p++)
                    if (dp[j - 1][p] != Long.MAX_VALUE)
                        dp[j][i] = Math.min(dp[j][i], Math.max(dp[j - 1][p], prefix[i] - prefix[p]));
        return (int) dp[k][n];
    }
}
```

**Key insight for the optimum.** Flip the question: "given a cap C, can we cut into at most k pieces?" Greedy answers it: extend the current piece while it fits, start a new piece when it would exceed C. Greedy is optimal for this check because cutting later never hurts. The answer is monotonic in C (a larger cap never needs more pieces), so binary search applies. "At most k" is enough: since n ≥ k, any piece can be split further without raising the maximum (values are non-negative).

## Solution

```java
class Solution {
    public int splitArray(int[] nums, int k) {
        long lo = 0, hi = 0;
        for (int x : nums) { lo = Math.max(lo, x); hi += x; }
        // Smallest cap such that a greedy left-to-right cut needs at most k pieces.
        while (lo < hi) {
            long mid = (lo + hi) >>> 1;
            if (pieces(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        return (int) lo;
    }

    private int pieces(int[] nums, long cap) {
        int count = 1;
        long cur = 0;
        for (int x : nums) {
            if (cur + x > cap) { count++; cur = 0; }
            cur += x;
        }
        return count;
    }
}
```

## Complexity

- **Binary search + greedy:** O(n · log(sum)), about 1000 × 30 steps. O(1) space.
- **DP:** O(k·n²) time, O(k·n) space (O(n) with two rows).

## Edge cases

- `k == n`: every element alone; answer = max element (`lo` starts there).
- `k == 1`: answer = total sum.
- Zeros in the array: pieces with sum 0 are fine.
- Lower bound must be the max element, otherwise a single element could exceed the cap and the greedy count would be meaningless.

## Variations

- **Capacity To Ship Packages Within D Days, Koko Eating Bananas, Painter's Partition:** the same "binary search on the answer + greedy feasibility" pattern.
- **Negative numbers:** monotonicity breaks; back to DP.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for presenting the DP first and then the faster idea.

Practise it in the app: Run / Submit on this page.
