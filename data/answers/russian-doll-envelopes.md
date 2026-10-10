**Short answer:** Sort envelopes by width ascending and, for equal widths, by height descending. The answer is then the longest strictly increasing subsequence of the heights, which you find in O(n log n) with the "tails + binary search" method. The descending tie-break stops two envelopes of the same width from both being picked.

## Picture it

Example 1: `[[5,4],[6,4],[6,7],[2,3]]`. After the sort (width up, equal widths by height down) the order is `[2,3], [5,4], [6,7], [6,4]`, so the heights are `3, 4, 7, 4`.

| Step | Envelope | Height h | Binary search: first tail ≥ h | tails after | len |
|---|---|---|---|---|---|
| 1 | [2,3] | 3 | none → index 0 (append) | [3] | 1 |
| 2 | [5,4] | 4 | none → index 1 (append) | [3, 4] | 2 |
| 3 | [6,7] | 7 | none → index 2 (append) | [3, 4, 7] | 3 |
| 4 | [6,4] | 4 | tails[1] = 4 → replace | [3, 4, 7] | 3 |

Answer 3: [2,3] ⊂ [5,4] ⊂ [6,7]. Because [6,4] comes *after* [6,7], it can only replace a tail, never extend a chain that already used width 6. Why the tie-break matters: for `[[3,4],[3,5]]` an ascending tie-break gives heights 4, 5 and a wrong LIS of 2; descending gives 5, 4 and the correct 1.

**The picture in one sentence:** sorting widths up and equal widths' heights down turns nesting into a strict LIS on heights, solved with tails plus binary search.

## Approach

- **DP baseline:** sort by width, then `dp[i] = 1 + max(dp[j])` over all j < i that fit strictly inside i. That is O(n²), too slow for 10⁵ envelopes.
- **Key insight:** after sorting by width, nesting only depends on heights being strictly increasing. That is the LIS problem. The trap is equal widths: [3,4] and [3,5] have increasing heights but do not nest. Sorting equal widths by height **descending** means an increasing run of heights can contain at most one envelope per width.
- **Optimal:** LIS with patience sorting. `tails[k]` is the smallest possible last height of an increasing chain of length k+1. For each height, binary-search the first tail that is `>= h` and replace it, or append. The final length of `tails` is the answer.

## Solution

```java
import java.util.*;

class Solution {
    public int maxEnvelopes(int[][] envelopes) {
        // Width ascending; equal widths by height descending.
        Arrays.sort(envelopes, (a, b) -> a[0] != b[0]
                ? Integer.compare(a[0], b[0])
                : Integer.compare(b[1], a[1]));
        int[] tails = new int[envelopes.length];
        int len = 0;
        for (int[] e : envelopes) {
            int lo = 0, hi = len;
            while (lo < hi) {                       // first tail >= height (strict LIS)
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < e[1]) lo = mid + 1; else hi = mid;
            }
            tails[lo] = e[1];
            if (lo == len) len++;
        }
        return len;
    }
}
```

## Complexity

- **Time:** O(n log n): the sort plus one binary search per envelope.
- **Space:** O(n) for `tails`, plus the sort's own space.

## Edge cases

- All envelopes identical: answer 1.
- All the same width: answer 1 (the descending tie-break guarantees it).
- A single envelope.
- Strictness: the binary search uses `<` so equal heights replace rather than extend.

## Follow-ups

- **Boxes may be rotated (2D):** normalise each box so `w <= h`, then run the same algorithm. If A fits in B in some orientation, it also fits when both are normalised, so nothing is lost.
- **3D boxes with rotation (box stacking):** sort each box's three dimensions, sort the boxes, and use the O(n²) DP that checks all three dimensions. The LIS trick does not extend cleanly to three strict dimensions.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for the O(n²) to O(n log n) walk.

Practise it in the app: Run / Submit on this page.
