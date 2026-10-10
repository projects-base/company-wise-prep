**Short answer:** Sort the array, then fix the first value `a[i]` and find pairs summing to `-a[i]` with two pointers on the rest. Skip repeated values for `i` and after each match, so every value triplet appears once. That is O(n²) time and needs no hash set of triplets.

## Picture it

Input `[-1,0,1,2,-1,-4]`, sorted to `a = [-4,-1,-1,0,1,2]` (indices 0–5).

| Step | i (a[i]) | lo (a[lo]) | hi (a[hi]) | Sum | Action |
|---|---|---|---|---|---|
| 1 | 0 (-4) | 1..4 | 5 (2) | -3, -3, -2, -1 | always < 0, so `lo++` until `lo == hi`; nothing found |
| 2 | 1 (-1) | 2 (-1) | 5 (2) | 0 | add `[-1,-1,2]`; no equal neighbours, so `lo=3`, `hi=4` |
| 3 | 1 (-1) | 3 (0) | 4 (1) | 0 | add `[-1,0,1]`; `lo=4`, `hi=3`, scan ends |
| 4 | 2 (-1) | – | – | – | `a[2] == a[1]`: skip, this first value was already done |
| 5 | 3 (0) | 4 (1) | 5 (2) | 3 | > 0, so `hi--`; `lo == hi`, scan ends |

Result: `[[-1,-1,2],[-1,0,1]]`.

**The picture in one sentence:** after sorting, each fixed `a[i]` turns the rest into a sorted Two Sum, where the sum's sign tells you which pointer to move.

## Approach

- **Brute force:** three nested loops, put each sorted triplet in a `Set`. O(n³) — too slow for n = 3000.
- **Hash map, O(n²):** fix `i`, then run Two Sum with a hash set over the rest. It works, but removing duplicates is awkward.
- **Key insight:** once the array is sorted, "find two numbers in a sorted range that sum to T" is a two-pointer scan. If the sum is too small, move `lo` right. If it is too big, move `hi` left. Sorting also puts equal values next to each other, so duplicates are skipped with a simple `a[k] == a[k-1]` check.
- **Optimal:** sort, loop `i`, two-pointer the suffix. Stop early when `a[i] > 0`, because three positive numbers cannot sum to 0.

## Solution

```java
import java.util.*;

class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        int[] a = nums.clone();
        Arrays.sort(a);
        int n = a.length;
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < n - 2; i++) {
            if (a[i] > 0) break;                        // all remaining values are positive
            if (i > 0 && a[i] == a[i - 1]) continue;    // same first value already handled
            int lo = i + 1, hi = n - 1;
            while (lo < hi) {
                int s = a[i] + a[lo] + a[hi];
                if (s < 0) lo++;
                else if (s > 0) hi--;
                else {
                    out.add(Arrays.asList(a[i], a[lo], a[hi]));
                    while (lo < hi && a[lo] == a[lo + 1]) lo++;   // skip equal seconds
                    while (lo < hi && a[hi] == a[hi - 1]) hi--;   // skip equal thirds
                    lo++;
                    hi--;
                }
            }
        }
        return out;
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort plus O(n²) for n two-pointer scans of O(n) each. Overall O(n²).
- **Space:** O(n) for the cloned array (O(log n) for the sort if you may sort the input in place), not counting the output.

## Edge cases

- All zeros, e.g. `[0,0,0,0]` → exactly one `[0,0,0]`.
- No answer, e.g. `[0,1,1]` → empty list.
- Many duplicates, e.g. `[-2,0,0,2,2]` → `[-2,0,2]` once.
- Sums stay within `int`: each value is at most 10⁵ in size.

## Follow-ups

- **4Sum and K-Sum:** recurse. `kSum(start, k, target)` fixes one value (skipping duplicates) and calls `kSum(i+1, k-1, target - a[i])`. The base case `k == 2` is the two-pointer scan. Time is O(n^(k-1)). Use `long` for the target, because 4Sum targets can overflow `int`.
- **How would you test it?** Unit tests for: the examples; all zeros; no solution; heavy duplicates (check no repeated triplet); only negatives or only positives; the minimum length of 3. Compare results against the brute-force O(n³) version on many random small arrays (property test). Add one large input (n = 3000) to check speed.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for the brute force → sort → two pointers progression.

Practise it in the app: Run / Submit on this page.
