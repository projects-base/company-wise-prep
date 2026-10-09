**Short answer:** DP over (day, energy). Let `dp[e]` be the best distance after the days so far, ending with energy e. Each day, from every reachable e you can rest (go to `min(k, e + 1)`, distance unchanged) or, if e ≥ 1, move (go to e − 1, add `a[i]`). The answer is the max over all energies after the last day. O(n · k) time and O(k) space, which is 10⁷ steps at the limits.

## Approach

- **Brute force:** try all 2ⁿ move/rest patterns. Exponential.
- **Greedy is tempting but wrong:** "move on the biggest days" ignores the energy timeline. With k = 1 you cannot move on two days in a row, and resting at full energy wastes a day. The constraint depends on order, so you need state.
- **Key insight:** the only thing that links days together is the current energy, and it has just k + 1 values. So the state is (day, energy), and the choice for one day only depends on that.
- **Rolling arrays:** each day depends only on the previous one, so keep two arrays of size k + 1.
- **Negative winds:** resting is always available, so you never have to move on a headwind day. The DP picks that automatically.

## Solution

```java
import java.util.Arrays;

class Solution {
    public long maxDistance(int[] a, int k) {
        final long NEG = Long.MIN_VALUE / 4;     // "unreachable", safe from overflow
        long[] dp = new long[k + 1];             // dp[e]: best distance ending the day with energy e
        Arrays.fill(dp, NEG);
        dp[k] = 0;                               // start full
        long[] next = new long[k + 1];
        for (int x : a) {
            Arrays.fill(next, NEG);
            for (int e = 0; e <= k; e++) {
                if (dp[e] == NEG) continue;
                int rest = Math.min(k, e + 1);
                next[rest] = Math.max(next[rest], dp[e]);             // rest
                if (e >= 1) next[e - 1] = Math.max(next[e - 1], dp[e] + x);  // move
            }
            long[] t = dp; dp = next; next = t;
        }
        long best = NEG;
        for (long v : dp) best = Math.max(best, v);
        return best;
    }
}
```

## Complexity

- **Time:** O(n · k): 10⁵ × 101 ≈ 10⁷ transitions.
- **Space:** O(k): two rolling arrays.

## Edge cases

- `k = 0`: you can never move; only resting is possible and the answer is 0.
- All winds negative or zero: rest every day, answer 0.
- Resting at full energy stays at k (the cap).
- Energy is never negative: the move transition requires e ≥ 1.
- Sums up to 10⁵ × 10⁴ = 10⁹ fit in `int`, but `long` leaves room and keeps the "unreachable" sentinel safe.

## Variations

- **Energy cost per move varies by day:** the transition becomes `e − cost[i]`; same DP.
- **Must end with full energy:** read `dp[k]` instead of the max.
- **k ≥ n:** you start with enough energy to move every day, so the answer is simply the sum of the positive `a[i]`. A quick shortcut worth stating before the DP.

Practise it in the app: Run / Submit on this page.
