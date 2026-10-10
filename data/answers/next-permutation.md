**Short answer:** Scan from the right for the first position i where `nums[i] < nums[i + 1]`; everything after i is non-increasing, so it is already the largest it can be. Swap `nums[i]` with the rightmost value after it that is larger than `nums[i]`, then reverse the suffix after i to make it as small as possible. If no such i exists, the whole array is non-increasing: reverse it all. O(n) time, O(1) space. 115 becomes 151.

## Picture it

`nums = [1,3,5,4,2]`:

```text
 index:  0  1  2  3  4
 nums:   1  3  5  4  2
            ^i       suffix 5 4 2 is non-increasing
                  ^j rightmost value > 3
 swap:   1  4  5  3  2
 reverse 2..4:   1  4  2  3  5
```

| Step | What | Result |
|---|---|---|
| 1 | i = 3: 4 ≥ 2, move left; i = 2: 5 ≥ 4, move left; i = 1: 3 < 5, stop | pivot i = 1 (value 3) |
| 2 | j = 4: 2 ≤ 3, move left; j = 3: 4 > 3, stop | j = 3 (value 4) |
| 3 | swap nums[1] and nums[3] | [1,4,5,3,2] |
| 4 | reverse the suffix from index 2 | [1,4,2,3,5] |

13542 → 14235, the next larger arrangement.

**The picture in one sentence:** find the rightmost place that can grow (just before the non-increasing suffix), bump it by the smallest larger value from the suffix, then make the suffix as small as possible by reversing it.

## Approach

- **Brute force:** generate all permutations, sort them, and take the one after the current. O(n!).
- **Key insight:** to get the *next* larger arrangement, change as far to the right as possible. A non-increasing suffix cannot be made larger on its own, so the change must happen just before it, at the pivot i.
- **Which value to swap in:** the smallest value in the suffix that is still larger than `nums[i]`. Because the suffix is non-increasing, that is the rightmost such value, found by scanning from the end.
- **Then minimise the suffix:** after the swap the suffix is still non-increasing, so reversing it makes it ascending, the smallest possible order. No sort needed.

## Solution

```java
class Solution {
    public void nextPermutation(int[] nums) {
        int i = nums.length - 2;
        while (i >= 0 && nums[i] >= nums[i + 1]) i--;          // find the pivot
        if (i >= 0) {
            int j = nums.length - 1;
            while (nums[j] <= nums[i]) j--;                    // rightmost value > pivot
            swap(nums, i, j);
        }
        for (int l = i + 1, r = nums.length - 1; l < r; l++, r--) swap(nums, l, r);  // reverse suffix
    }

    private void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }
}
```

Walkthrough for `[1, 1, 5]`: the pivot is index 1 (1 < 5); the rightmost value > 1 is 5 at index 2; swap → `[1, 5, 1]`; reversing the one-element suffix changes nothing.

## Complexity

- **Time:** O(n): at most three linear passes.
- **Space:** O(1), in place.

## Edge cases

- Already the largest (`[3, 2, 1]`): no pivot, `i = −1`, the whole array is reversed to `[1, 2, 3]`.
- Duplicates: the strict comparisons (`>=` when finding the pivot, `<=` when finding j) make it skip equal values, which is what keeps `[1, 5, 1]` → `[5, 1, 1]` correct.
- Single element: unchanged.
- Interview phrasing "return the same number if none exists": check for `i < 0` and return early instead of reversing.

## Variations

- **Next Greater Element III (LeetCode 556):** input is an `int`. Convert to a digit array, apply this, convert back in a `long`, and return −1 if the result exceeds `Integer.MAX_VALUE` or no larger arrangement exists.
- **Previous permutation:** mirror every comparison (pivot where `nums[i] > nums[i + 1]`, swap with the rightmost smaller value, reverse).
- **k-th permutation:** use factorials to pick each position directly, instead of calling next k times.
- **Generate all permutations in order:** start sorted and call this until it wraps around; it handles duplicates without producing repeats.

Practise it in the app: Run / Submit on this page.
