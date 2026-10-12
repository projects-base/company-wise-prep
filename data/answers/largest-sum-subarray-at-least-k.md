**Short answer:** Any subarray with at least `k` elements ending at index `i` is "the last `k` elements" plus, optionally, some run of elements just before them. So compute Kadane's `best[j]` (the maximum sum of a subarray ending at `j`), slide a window of exactly `k`, and at each `i` take `max(window, window + best[i − k])`. The answer is the maximum over all `i`. O(n) time, O(n) space (O(1) with the prefix-sum version).

## Picture it

`a = [3, -1, 2, 1]`, `k = 2`. First Kadane, then the window.

| i | a[i] | best[i] = max(a[i], best[i−1] + a[i]) |
|---|---|---|
| 0 | 3 | 3 |
| 1 | -1 | max(-1, 2) = 2 |
| 2 | 2 | max(2, 4) = 4 |
| 3 | 1 | max(1, 5) = 5 |

| i | Window of k ending at i | window | best[i − k] | window + best[i − k] | Best so far |
|---|---|---|---|---|---|
| 1 | [3, -1] | 2 | none | | 2 |
| 2 | [-1, 2] | 1 | best[0] = 3 | 4 (that is `3, -1, 2`) | 4 |
| 3 | [2, 1] | 3 | best[1] = 2 | 5 (that is `3, -1, 2, 1`) | **5** |

Answer 5, the whole array. A plain window of size 2 alone would give only 3.

**The picture in one sentence:** split every valid subarray into "exactly the last k" plus "the best tail before them", and Kadane already knows the best tail.

## Approach

**Brute force.** Try every start and every end with length `≥ k`, keeping a running sum: O(n²). Fine for small inputs, too slow for 10⁵.

**Key insight.** A subarray `[s..i]` with length `≥ k` = the fixed block `[i−k+1..i]` + an optional extension `[s..i−k]`. The extension must end exactly at `i − k`, and the best such extension is `best[i − k]` from Kadane. If that is negative, skip it (the `max` handles that).

**Prefix-sum alternative (O(1) extra space).** With `P[j]` = sum of the first `j` elements, the sum of `[s..i]` is `P[i+1] − P[s]` and we need `s ≤ i + 1 − k`. So keep the minimum prefix seen up to `P[i+1−k]` and maximise `P[i+1] − minPrefix`. Same answer, one pass.

## Solution

```java
class Solution {
    public long maxSumAtLeastK(int[] a, int k) {
        int n = a.length;
        if (k <= 0 || k > n) throw new IllegalArgumentException("need 1 <= k <= n");

        long[] best = new long[n];                   // best[j]: max sum of a subarray ending at j
        best[0] = a[0];
        for (int j = 1; j < n; j++) best[j] = Math.max(a[j], best[j - 1] + a[j]);

        long window = 0;
        for (int j = 0; j < k; j++) window += a[j];  // window ending at index k - 1
        long ans = window;

        for (int i = k; i < n; i++) {
            window += a[i] - a[i - k];                // slide: now covers [i - k + 1 .. i]
            ans = Math.max(ans, Math.max(window, window + best[i - k]));
        }
        return ans;
    }

    // Same result with prefix sums and O(1) extra space.
    public long maxSumAtLeastKPrefix(int[] a, int k) {
        long prefix = 0, lagPrefix = 0, minPrefix = 0;  // minPrefix starts as P[0] = 0
        for (int j = 0; j < k; j++) prefix += a[j];
        long ans = prefix;                               // P[k] - P[0]
        for (int i = k; i < a.length; i++) {
            prefix += a[i];                           // P[i + 1]
            lagPrefix += a[i - k];                    // P[i + 1 - k]
            minPrefix = Math.min(minPrefix, lagPrefix);
            ans = Math.max(ans, prefix - minPrefix);
        }
        return ans;
    }
}
```

## Complexity

- **Time:** O(n). One Kadane pass and one sliding pass.
- **Space:** O(n) for `best` in the first version; O(1) in the prefix-sum version.

## Edge cases

- All negative, e.g. `[-4, -2, 1, -3]`, `k = 2`: the answer is -1 (`[-2, 1]`). Never start `ans` at 0, or you return an empty subarray's sum.
- `k = n`: only the whole array qualifies; the slide loop never runs.
- `k = 1`: it becomes plain Kadane.
- Large values: use `long` sums; `int` can overflow with 10⁵ elements of 10⁹.
- `k > n`: no valid subarray. Agree with the interviewer whether to throw or return a sentinel.

## Variations

- **Return the subarray, not just the sum:** also track the start index for each `best[j]`.
- **At most `k` elements:** different problem; use prefix sums with a monotonic deque of minimum prefixes over the last `k` positions.
- **Exactly `k`:** just the sliding window.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for going from O(n²) to O(n) out loud.
