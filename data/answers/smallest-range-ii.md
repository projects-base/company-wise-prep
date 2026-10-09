**Short answer:** Sort the array. In an optimal answer, some prefix of the sorted array goes up by k and the rest goes down by k. So try every split point i: the new maximum is `max(a[i] + k, a[n-1] - k)` and the new minimum is `min(a[0] + k, a[i+1] - k)`. Take the best difference, also counting "everyone moves the same way" (the original range). O(n log n) for the sort, then O(n).

## Approach

- **Brute force:** try all 2ⁿ choices. Impossible.
- **Key insight:** if a smaller element goes down while a larger one goes up, swapping their choices never makes the range worse. So after sorting, the optimal choice is "the first i+1 elements go up, the rest go down" for some split i.
- **Given a split i:**
  - The candidates for the maximum are `a[i] + k` (the largest of the raised group) and `a[n-1] - k` (the largest of the lowered group).
  - The candidates for the minimum are `a[0] + k` (the smallest raised) and `a[i+1] - k` (the smallest lowered).
- Start `best` at `a[n-1] - a[0]`, which covers all-up or all-down (both keep the original range).

## Solution

```java
import java.util.*;

class Solution {
    public int smallestRangeII(int[] nums, int k) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length;
        int best = a[n - 1] - a[0];              // everyone moves the same way
        for (int i = 0; i + 1 < n; i++) {        // a[0..i] go up, a[i+1..] go down
            int hi = Math.max(a[i] + k, a[n - 1] - k);
            int lo = Math.min(a[0] + k, a[i + 1] - k);
            best = Math.min(best, hi - lo);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort; the scan is O(n).
- **Space:** O(n) for the sorted copy (O(log n) if sorting in place is allowed).

## Edge cases

- One element: 0.
- `k == 0`: the original range.
- k larger than the range: moving everything the same way keeps the original range, which is then the best.
- All equal values: 0.
- Values up to 10⁴ and k up to 10⁴, so `int` arithmetic cannot overflow.

## Variations

- **Smallest Range I** (each element may move by any amount in [-k, k]): the answer is just `max(0, max - min - 2k)`, no sorting needed.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for turning an exchange argument into a linear scan.

Practise it in the app: Run / Submit on this page.
