**Short answer:** The retained total `f(X) = Σ min(count, X)` never decreases as X grows, so binary search for the largest X with `f(X) ≤ maxEntries`. If the full total already fits, return −1. Each check is O(n) and X goes up to 10⁹, so about 30 checks: O(n log M). Sorting and sweeping gives O(n log n) without searching.

## Approach

- **Brute force:** try X = 0, 1, 2, … until the total exceeds the limit. Up to 10⁹ values of X, each O(n). Far too slow.
- **Key insight:** f is monotonic (non-decreasing). Monotonic "fits / does not fit" means binary search on the answer. The answer is in `[0, max − 1]`: f(0) = 0 always fits, and f(max) = total does not fit (otherwise we returned −1).
- **Sort-and-sweep alternative:** sort counts ascending. If the first i categories are kept whole and the other n − i are capped at X, then `f(X) = prefix[i] + (n − i) · X` for `X` between `c[i−1]` and `c[i]`. Walk i upward; at each step solve for the largest X directly: `X = (maxEntries − prefix[i]) / (n − i)`, and accept it if it is less than `c[i]`. O(n log n) for the sort, O(n) for the sweep.

## Solution

```java
class Solution {
    public int maxRetentionCap(int[] counts, long maxEntries) {
        long total = 0;
        int max = 0;
        for (int c : counts) { total += c; max = Math.max(max, c); }
        if (total <= maxEntries) return -1;                 // no cap needed

        int lo = 0, hi = max - 1;                           // answer in [0, max - 1]
        while (lo < hi) {
            int mid = lo + (hi - lo + 1) / 2;               // upper mid: we look for the LAST fit
            if (retained(counts, mid) <= maxEntries) lo = mid; else hi = mid - 1;
        }
        return lo;
    }

    private static long retained(int[] counts, int x) {
        long s = 0;
        for (int c : counts) s += Math.min(c, x);
        return s;
    }
}
```

## Complexity

- **Time:** O(n log M), M = largest count (about 30 iterations × 10⁵).
- **Space:** O(1).

## Edge cases

- Everything fits: −1 (be clear with the interviewer whether "no cap" should be −1, `max`, or infinity).
- `maxEntries = 0`: X = 0.
- `maxEntries < n` with all counts ≥ 1: X = 0, because X = 1 already keeps n entries.
- Overflow: totals up to 10⁵ × 10⁹ = 10¹⁴ need `long`; so does `maxEntries`.
- Binary search for the *last* true value: use the upper middle (`lo + (hi − lo + 1) / 2`) or the loop never ends when `hi = lo + 1`.

## Variations

- **Many queries with different limits:** sort once and precompute prefix sums; each query is a binary search over the sorted counts plus one division, O(log n).
- **Per-category weights or minimums:** still monotonic in X, so the same binary search works with a different `retained`.
- This is the same shape as Koko Eating Bananas and "Capacity to Ship Packages": a monotonic check plus binary search on the answer.

Practise it in the app: Run / Submit on this page.
