**Short answer:** A peak is an element strictly greater than its neighbours, and positions outside the array count as minus infinity, so a peak always exists. Binary search on the slope: compare `nums[mid]` with `nums[mid + 1]`. If the next element is bigger, we are going uphill, so a peak must exist to the right; otherwise one exists at `mid` or to the left. That is O(log n) time and O(1) space, even though the array is not sorted.

## Approach

- **Brute force:** scan once and return the first `i` with `nums[i] > nums[i + 1]` (or the last index). O(n). Fine, but the question asks for O(log n).
- **Key insight:** binary search does not need a sorted array. It needs a rule that says which half *must* contain an answer. If `nums[mid] < nums[mid + 1]`, walk right from `mid + 1`. Either the values keep rising until the end (the last element is a peak, because the boundary is minus infinity) or they drop somewhere (the point before the drop is a peak). Either way a peak is in `[mid + 1, hi]`. Symmetric reasoning gives `[lo, mid]` in the other case.
- **Optimal:** shrink `[lo, hi]` with that rule until `lo == hi`. That index is a peak.

LeetCode 162 guarantees `nums[i] != nums[i + 1]`, which the argument relies on.

## Solution

```java
class Solution {
    public int findPeakElement(int[] nums) {
        int lo = 0, hi = nums.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;      // mid < hi, so mid + 1 is in range
            if (nums[mid] < nums[mid + 1]) {
                lo = mid + 1;                  // rising: a peak is to the right
            } else {
                hi = mid;                      // falling: mid could be the peak
            }
        }
        return lo;
    }
}
```

## Complexity

- **Time:** O(log n). The range halves each iteration.
- **Space:** O(1).

## Edge cases

- One element: the loop does not run, return 0.
- Strictly increasing array: the answer is the last index.
- Strictly decreasing array: the answer is 0.
- Several peaks: any one is accepted. Say this out loud, because the binary search may not return the first or the highest.
- Equal neighbours (not allowed in the LeetCode version): the slope rule breaks, and with plateaus the worst case falls back to O(n).

## Variations

- **Peak in a mountain array** (LeetCode 852, exactly one peak): the same code.
- **Find a peak in a 2D grid** (LeetCode 1901): binary search on columns, take the max of the middle column, then move toward the larger neighbour. O(m log n).
- **Local minimum:** the same idea with the comparison flipped.

Further reading: [C4 · The optimisation playbook](../academy/lessons/C4.md).
