**Short answer:** Count, for each end index i, how many valid subarrays end at i. Track three positions: the last "bad" element (outside `[minK, maxK]`), the last occurrence of `minK` and the last occurrence of `maxK`. A subarray ending at i is valid if it starts after the last bad index and at or before both last hits, so it adds `max(0, min(lastMin, lastMax) − lastBad)`. One pass, O(n) time, O(1) space.

## Picture it

`nums = [2,1,5,3,1,7,5,1]`, `minK = 1`, `maxK = 5`. The 7 at index 5 is "bad".

| i | nums[i] | lastBad | lastMin | lastMax | Adds `min(lastMin, lastMax) − lastBad` | total | New subarrays ending at i |
|---|---|---|---|---|---|---|---|
| 0 | 2 | -1 | -1 | -1 | -1 − (-1) = 0 | 0 | – |
| 1 | 1 | -1 | 1 | -1 | -1 − (-1) = 0 | 0 | no 5 yet |
| 2 | 5 | -1 | 1 | 2 | 1 − (-1) = 2 | 2 | [2,1,5], [1,5] |
| 3 | 3 | -1 | 1 | 2 | 1 − (-1) = 2 | 4 | [2,1,5,3], [1,5,3] |
| 4 | 1 | -1 | 4 | 2 | 2 − (-1) = 3 | 7 | starts 0, 1, 2 |
| 5 | 7 | 5 | 4 | 2 | 2 − 5 < 0 → 0 | 7 | 7 is out of range |
| 6 | 5 | 5 | 4 | 6 | 4 − 5 < 0 → 0 | 7 | no 1 after the 7 yet |
| 7 | 1 | 5 | 7 | 6 | 6 − 5 = 1 | **8** | [5,1] |

**The picture in one sentence:** for each right end, the valid starts are exactly the range after the last bad index up to the earlier of the last `minK` and last `maxK`, so three remembered indices count them all.

## Approach

**Brute force.** Try every start, extend the end while tracking running min and max, and count matches. O(n²); too slow for 10⁵ elements.

**Key insight.** Fix the right end i and ask which starts work. A start s is valid exactly when:

- no element in `[s, i]` is below `minK` or above `maxK` → s > `lastBad`;
- the window contains a `minK` → s ≤ `lastMin`;
- the window contains a `maxK` → s ≤ `lastMax`.

So valid starts form the range `(lastBad, min(lastMin, lastMax)]`, and its size is `min(lastMin, lastMax) − lastBad` when positive. No window shrinking is needed; we just remember three indices.

**Optimal.** One left-to-right pass updating the three indices, then adding the range size.

## Solution

```java
import java.util.*;

class Solution {
    public long countSubarrays(int[] nums, int minK, int maxK) {
        long total = 0;
        int lastBad = -1, lastMin = -1, lastMax = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] < minK || nums[i] > maxK) lastBad = i;
            if (nums[i] == minK) lastMin = i;
            if (nums[i] == maxK) lastMax = i;
            // Subarrays ending at i are valid when they start after lastBad and at or before both last hits.
            total += Math.max(0, Math.min(lastMin, lastMax) - lastBad);
        }
        return total;
    }
}
```

Trace on `[1,3,5,2,7,5]`, minK = 1, maxK = 5: at i = 2 (value 5) lastMin = 0, lastMax = 2, lastBad = −1 → +1 ([1,3,5]). At i = 3 → +1 ([1,3,5,2]). At i = 4 (7) lastBad = 4 → +0 from then on. Total 2.

## Complexity

- **Time:** O(n) — one pass.
- **Space:** O(1) — three indices and a counter.

## Edge cases

- `minK == maxK`: both last indices update together; `[1,1,1,1]` gives 1 + 2 + 3 + 4 = 10.
- `minK > maxK`: every element is "bad" (it cannot be ≥ minK and ≤ maxK at once), so the answer is 0 automatically.
- Neither bound seen yet: `min(lastMin, lastMax)` is −1, the difference is ≤ 0 and `max(0, …)` clamps it.
- The answer can reach about n²/2 ≈ 5·10⁹, so the total must be `long`.

## Variations

- **Subarrays with exactly K distinct integers:** count "at most K" minus "at most K−1" with a sliding window — another way of turning "exactly" into differences.
- **Count subarrays where max is in a range** (LeetCode 795): the same last-bad-index trick.

See also [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
