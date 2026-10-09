**Short answer:** The versions form a sorted boolean sequence: all good, then all bad. Binary search for the boundary: if `mid` is bad, the answer is `mid` or earlier; if it is good, the answer is after `mid`. That takes about 31 API calls for `n` up to 2³¹ − 1. Compute the midpoint as `lo + (hi - lo) / 2` so it never overflows.

## Approach

- **Brute force:** test `1, 2, 3, ...` until one is bad. O(n) calls, up to two billion. Too many.
- **Key insight:** `isBadVersion` is monotone (false...false, true...true). Any monotone predicate can be searched with binary search for its first `true`.
- **Optimal:** keep the invariant "the first bad version is in `[lo, hi]`". Start with `[1, n]`. While `lo < hi`, probe `mid`. Bad means `hi = mid` (mid could be the answer, keep it). Good means `lo = mid + 1`. When `lo == hi` the range has one element, which is the answer.

## Solution

```java
class Solution extends VersionControl {
    public int firstBadVersion(int n) {
        int lo = 1, hi = n;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;   // never overflows
            if (isBadVersion(mid)) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }
}
```

`mid` is always strictly less than `hi` (it rounds down), so `hi = mid` always shrinks the range and the loop cannot spin forever.

## Complexity

- **Time O(log n):** about 31 calls for `n = 2³¹ − 1`, inside the 40-call limit.
- **Space O(1).**

## Edge cases

- `n = 1`: the loop does not run, return 1 with zero calls.
- First version is bad: `hi` keeps moving down to 1.
- Last version is bad and `n = Integer.MAX_VALUE`: `(lo + hi) / 2` would overflow here; the safe form does not.

## Follow-ups

- **Speed it up with multiple threads.** If calls are slow and independent (say each one runs a CI build), use k-ary search: with `k` threads, probe `k` evenly spaced versions in `[lo, hi]` in parallel, find the first bad among them, and narrow the range to the gap just before it. Each round divides the range by `k + 1`, so rounds drop from `log₂ n` to `log_(k+1) n`. Total calls go up; wall-clock time goes down. In Java, submit the probes to an `ExecutorService` (or virtual threads) and join the futures.
- **Avoid overflow when l/r are huge.** `lo + (hi - lo) / 2`, or `(lo + hi) >>> 1` (unsigned shift treats the overflowed sum correctly as long as both are non-negative ints). For values beyond `int`, use `long`.

## Variations

- Search Insert Position, first/last occurrence in a sorted array, "binary search on the answer" problems: same boundary template.

Practise it in the app: Run / Submit on this page.
