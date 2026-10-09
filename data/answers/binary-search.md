**Short answer:** Keep a search range `[lo, hi]` over a sorted array. Look at the middle element: if it equals the target, return it. If it is smaller, the target can only be to the right, so drop the left half; otherwise drop the right half. Each step halves the range, so it takes O(log n) time and O(1) space.

## Approach

- **Brute force:** scan every element. O(n). It ignores the fact that the array is sorted.
- **Key insight:** in a sorted array, one comparison with the middle tells you which half cannot contain the target. Throw that half away.
- **Optimal:** iterative binary search with a clear loop invariant. With `lo <= hi`, the invariant is "if the target exists, it is inside `[lo, hi]`". The loop ends when the range is empty.

Compute the middle as `lo + (hi - lo) / 2` (or `(lo + hi) >>> 1`) so `lo + hi` cannot overflow `int`.

## Solution

```java
class Solution {
    public int search(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) return mid;
            if (nums[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    // First index with nums[i] >= target (lower bound). Returns n if none.
    public int lowerBound(int[] nums, int target) {
        int lo = 0, hi = nums.length;          // half-open [lo, hi)
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    // First index with nums[i] > target (upper bound).
    public int upperBound(int[] nums, int target) {
        int lo = 0, hi = nums.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] <= target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
```

## Complexity

- **Time:** O(log n). The range halves every iteration.
- **Space:** O(1). Only a few indices. A recursive version uses O(log n) stack.

## Edge cases

- Empty array: `hi = -1`, the loop never runs, return -1.
- One element, target present or absent.
- Target smaller than all or larger than all elements.
- Duplicates: plain search returns *some* matching index, not necessarily the first. Use the bounds for that.
- Off-by-one: mixing `lo <= hi` with `hi = mid` can loop forever. Pick one convention (closed `[lo, hi]` or half-open `[lo, hi)`) and keep it.

## Variations

- **First and last occurrence:** first = `lowerBound(t)`, check it is in range and equals `t`. Last = `upperBound(t) - 1`. Count of `t` = `upperBound - lowerBound`.
- **Binary search on the answer:** when the answer is a number and a check `feasible(x)` is monotonic (false, false, ..., true, true). Example: the minimum ship capacity to deliver packages in `D` days. Search capacity in `[max(weights), sum(weights)]`. For each `mid`, greedily count days needed. If it fits, `hi = mid`, else `lo = mid + 1`. Time O(n log(sum)).
- In Java you can also use `Arrays.binarySearch`. It returns `-(insertionPoint) - 1` when the key is missing, and gives no guarantee which duplicate it finds.

Further reading: [C4 · The optimisation playbook](../academy/lessons/C4.md).
