**Short answer:** Binary search for a partition, on the shorter array. Take i elements from `nums1` and `j = half − i` from `nums2` into a "left half". The partition is right when `nums1[i−1] ≤ nums2[j]` and `nums2[j−1] ≤ nums1[i]`. Then the median is the max of the left side (odd total) or the average of the left max and the right min (even total). O(log(min(m, n))).

## Approach

- **Brute force:** merge both arrays and pick the middle. O(m + n) time and space. A two-pointer walk to the middle without storing is O(m + n) time, O(1) space.
- **Key insight:** the median splits the combined values into a left half and a right half of equal size (left gets the extra one if the total is odd). The left half takes some prefix of `nums1` and some prefix of `nums2`. Choosing i fixes j, so there is one free variable.
- **Monotonic test:** if `nums1[i−1] > nums2[j]`, i is too large; if `nums2[j−1] > nums1[i]`, i is too small. That is a binary search on i over `[0, m]`.
- **Search the shorter array:** it keeps `j` in range and makes the cost O(log min(m, n)).

## Solution

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
        int m = nums1.length, n = nums2.length, half = (m + n + 1) / 2;
        int lo = 0, hi = m;
        while (lo <= hi) {
            int i = (lo + hi) / 2;          // taken from nums1 into the left half
            int j = half - i;               // taken from nums2
            int aLeft  = i == 0 ? Integer.MIN_VALUE : nums1[i - 1];
            int aRight = i == m ? Integer.MAX_VALUE : nums1[i];
            int bLeft  = j == 0 ? Integer.MIN_VALUE : nums2[j - 1];
            int bRight = j == n ? Integer.MAX_VALUE : nums2[j];
            if (aLeft > bRight) hi = i - 1;          // took too many from nums1
            else if (bLeft > aRight) lo = i + 1;     // took too few
            else {
                int leftMax = Math.max(aLeft, bLeft);
                if ((m + n) % 2 == 1) return leftMax;
                int rightMin = Math.min(aRight, bRight);
                return (leftMax + (double) rightMin) / 2.0;
            }
        }
        throw new IllegalStateException("arrays are not sorted");
    }
}
```

## Complexity

- **Time:** O(log min(m, n)).
- **Space:** O(1) (the swap is one extra call, not a copy).

## Edge cases

- One array empty: m = 0, so i = 0 and the answer comes straight from `nums2`.
- Partition at an edge (i = 0 or i = m): the sentinels `MIN_VALUE` / `MAX_VALUE` stand in for "nothing there".
- Duplicates across arrays: the `≤` conditions allow equal values on both sides.
- Even total: average in `double` (`leftMax + (double) rightMin`), so the sum cannot overflow `int` and `2 + 3` gives 2.5, not 2.
- Total of 1: `half = 1`, odd case.

## Variations

- **K-th smallest of two sorted arrays:** the same partition with `half = k`, or the recursive "discard k/2 elements" method, O(log k).
- **Median of k sorted arrays:** binary search on the value and count elements ≤ it in each array, O(k · log n · log V).
- **Running median of a stream:** two heaps (max-heap for the lower half, min-heap for the upper).

Practise it in the app: Run / Submit on this page.
