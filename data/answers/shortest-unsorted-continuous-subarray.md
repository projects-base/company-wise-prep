**Short answer:** The right end of the subarray is the last index whose value is smaller than the maximum seen to its left. The left end is the first index whose value is larger than the minimum seen to its right. One pass from each side (or both in the same loop) finds them in O(n) time and O(1) space.

## Approach

- **Brute force:** sort a copy and compare it with the original. The first and last positions that differ bound the answer. O(n log n) time, O(n) space, and perfectly acceptable as a first answer.
- **Key insight:** an element at index i is already in its final place on the right side only if it is at least as big as everything before it. So scanning left to right with a running max, any `nums[i] < max` must be inside the subarray, and the **last** such i is the right end. Symmetrically, scanning right to left with a running min, the **last** j seen (the leftmost) with `nums[j] > min` is the left end.
- **Optimal:** do both scans in one loop. Initialise `start = 0, end = -1` so a sorted array gives `end - start + 1 = 0`.

## Solution

```java
class Solution {
    public int findUnsortedSubarray(int[] nums) {
        int n = nums.length;
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, start = 0, end = -1;
        for (int i = 0; i < n; i++) {
            // Left to right: anything below the running max is out of place.
            if (nums[i] < max) end = i; else max = nums[i];
            // Right to left: anything above the running min is out of place.
            int j = n - 1 - i;
            if (nums[j] > min) start = j; else min = nums[j];
        }
        return end - start + 1;
    }
}
```

## Complexity

- **Time:** O(n), one loop.
- **Space:** O(1).

## Edge cases

- Already sorted, or one element: 0 (the `start = 0, end = -1` initialisation).
- Fully reversed: the whole array.
- Duplicates such as `[1,3,2,2,2]`: the comparisons are strict, so equal values are not wrongly flagged, but the trailing 2s are correctly included because they are below the max 3. Answer 4.
- Negative values: the sentinels `MIN_VALUE` / `MAX_VALUE` handle them.

## Variations

- A monotonic-stack solution also runs in O(n) but uses O(n) space; the two-scan version is simpler.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for presenting sort-and-compare first, then the O(n) pass.

Practise it in the app: Run / Submit on this page.
