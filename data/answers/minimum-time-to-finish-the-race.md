**Short answer:** Split it into two steps. First compute `best[k]`: the fastest time to run k consecutive laps on one fresh tyre, over all tyre types. Because r ≥ 2, lap times double at least every lap, so after about 20 laps changing tyres is always better; stop extending once a lap costs more than `changeTime + f`. Then a 1-D DP: `dp[i] = min over k of dp[i − k] + changeTime + best[k]`, with no change cost before the first stint. O(T · 20 + numLaps · 20).

## Picture it

Example 1: `tires = [[2,3],[3,4]]`, `changeTime = 5`, `numLaps = 4`.

Step 1, the cheapest stint of k laps on one fresh tyre:

| k | Tyre [2,3]: laps 2, 6, 18 | Tyre [3,4]: laps 3, 12 | best[k] |
|---|---|---|---|
| 1 | 2 | 3 | 2 |
| 2 | 2 + 6 = 8 | 3 + 12 = 15 (then 12 ≥ 5 + 3, stop) | 8 |
| 3 | 8 + 18 = 26 (then 18 ≥ 5 + 2, stop) | — | 26 |
| 4 | — | — | ∞ |

Step 2, `dp[i] = min over k of best[k] + (k == i ? 0 : 5 + dp[i − k])`:

| i | k = 1 | k = 2 | k = 3 | dp[i] |
|---|---|---|---|---|
| 1 | 2 | — | — | 2 |
| 2 | 2 + 5 + 2 = 9 | 8 | — | 8 |
| 3 | 2 + 5 + 8 = 15 | 8 + 5 + 2 = 15 | 26 | 15 |
| 4 | 2 + 5 + 15 = 22 | 8 + 5 + 8 = **21** | 26 + 5 + 2 = 33 | 21 |

Each `dp[i]` depends on `dp[i − k]` for the few k where `best[k]` is finite. Answer 21: two 2-lap stints on [2,3] with one change.

**The picture in one sentence:** collapse all tyres into one short `best[k]` table of stint costs, then a 1-D DP chooses the length of the last stint.

## Approach

- **Brute force:** decide at every lap whether to change and to which tyre. Exponential.
- **Key insight 1:** the race is a sequence of *stints*, each on one fresh tyre. The cost of a stint of k laps depends only on k and the tyre type, so precompute `best[k]` = cheapest stint of length k over all types. The 10⁵ tyre types collapse into one small array.
- **Key insight 2:** stints are short. If lap j on a tyre costs `f · r^(j−1) ≥ changeTime + f`, then changing to a fresh tyre of the same type and running that lap at cost `f` is at least as good. With r ≥ 2 that happens within about 18 laps (2¹⁷ > 10⁵ + 10⁵). So `best[k]` is finite only for small k, and the DP inner loop is short.
- **DP:** `dp[i]` = minimum time for i laps. The last stint has length k: `dp[i] = dp[i − k] + changeTime + best[k]`, except when k = i (the first stint has no change).

## Solution

```java
import java.util.Arrays;

class Solution {
    public int minimumFinishTime(int[][] tires, int changeTime, int numLaps) {
        final long INF = Long.MAX_VALUE / 4;
        long[] best = new long[numLaps + 1];        // best[k]: k laps on one fresh tyre
        Arrays.fill(best, INF);
        for (int[] t : tires) {
            long f = t[0], r = t[1];
            long lap = f, total = 0;
            for (int k = 1; k <= numLaps; k++) {
                total += lap;
                if (total < best[k]) best[k] = total;
                if (lap >= changeTime + f) break;  // a fresh tyre would beat the next lap
                lap *= r;
                if (lap > INF / 2) break;
            }
        }
        long[] dp = new long[numLaps + 1];
        for (int i = 1; i <= numLaps; i++) {
            dp[i] = INF;
            for (int k = 1; k <= i; k++) {
                if (best[k] >= INF) break;          // no tyre runs k laps usefully
                long cand = best[k] + (k == i ? 0 : changeTime + dp[i - k]);
                dp[i] = Math.min(dp[i], cand);
            }
        }
        return (int) dp[numLaps];
    }
}
```

## Complexity

- **Time:** O(T · L + numLaps · L), where T is the number of tyre types and L ≈ 20 is the longest useful stint.
- **Space:** O(numLaps).

## Edge cases

- One lap: the smallest `f` over all tyres.
- Large `changeTime`: long stints become worthwhile; the cap `lap ≥ changeTime + f` still bounds them because lap times grow geometrically.
- Overflow: `f · r^k` explodes fast (with f = r = 10⁵, the 4th lap alone is 10²⁰, beyond `long`), so compute in `long` and break early; in practice the `changeTime + f` check stops it on the 2nd lap. The final answer fits in `int` per the constraints.
- `best[k]` is non-decreasing in k, and once it is INF it stays INF, so the `break` in the DP loop is safe.

## Variations

- **Tyres with limited copies:** the stint costs now depend on which tyres remain; the state needs counts, much harder.
- **Change time varies by lap:** use `dp[i − k] + change[i − k] + best[k]`.
- **Unbounded knapsack framing:** `best[k]` are item weights (laps) with costs; the DP is a cover-exactly-N-laps knapsack, which is a helpful way to explain it.

Practise it in the app: Run / Submit on this page.
