**Short answer:** We want `nums[0] <= nums[1] >= nums[2] <= nums[3] ...`. One greedy pass does it: at each index `i`, if `i` is odd and `nums[i] < nums[i-1]`, or `i` is even and `nums[i] > nums[i-1]`, swap the two. A swap fixes the current pair without breaking the previous one. O(n) time, O(1) space, no sorting.

## Picture it

`nums = [3,5,2,1,6,4]`. Odd `i` needs `nums[i] >= nums[i-1]`; even `i` needs `nums[i] <= nums[i-1]`.

| i | Parity | Pair (nums[i-1], nums[i]) | Violation? | Array after |
|---|---|---|---|---|
| 1 | odd | (3, 5) | no | [3,5,2,1,6,4] |
| 2 | even | (5, 2) | no | [3,5,2,1,6,4] |
| 3 | odd | (2, 1) | 1 < 2, swap | [3,5,1,2,6,4] |
| 4 | even | (2, 6) | 6 > 2, swap | [3,5,1,6,2,4] |
| 5 | odd | (2, 4) | no | **[3,5,1,6,2,4]** |

Result: 3 ≤ 5 ≥ 1 ≤ 6 ≥ 2 ≤ 4. The swap at i = 4 put a larger value at index 3, which only made `5 ≥ 1 ≤ 6` stronger.

**The picture in one sentence:** fix each adjacent pair with one swap as you go, because the swap always moves the value at `i - 1` in the direction its earlier constraint already wanted.

## Approach

- **Sort first:** sort the array, then swap pairs `(1,2), (3,4), ...`. After sorting, `a[1] <= a[2]`, and swapping makes position 1 the larger. O(n log n). A fine first answer.
- **Key insight:** local fixes are enough. Suppose the prefix up to `i - 1` is valid and `i` is odd, so we need `nums[i] >= nums[i-1]`. If not, swap them. Position `i - 1` is even, so it must stay `<=` its left neighbour `nums[i-2]`. Before the swap that held. The swap puts a *smaller* value at `i - 1`, so it still holds. The even case is symmetric. So one left-to-right pass works.
- **Optimal:** the greedy pass.

## Solution

```java
class Solution {
    public void wiggleSort(int[] nums) {
        for (int i = 1; i < nums.length; i++) {
            boolean odd = (i & 1) == 1;
            if ((odd && nums[i] < nums[i - 1]) || (!odd && nums[i] > nums[i - 1])) {
                int t = nums[i];
                nums[i] = nums[i - 1];
                nums[i - 1] = t;
            }
        }
    }
}
```

## Complexity

- **Time:** O(n). One pass, constant work per index.
- **Space:** O(1). In place.

## Edge cases

- Empty or one element: nothing to do.
- All equal values: already valid, since the inequalities are non-strict.
- Many answers are valid. Any arrangement that satisfies the pattern is accepted.

## Variations

- **Wiggle Sort II (LeetCode 324), strict `<` and `>`:** the greedy swap fails with duplicates. On `[1, 1, 1, 2, 2, 2]` it never swaps anything (equal neighbours never trigger a swap), although `[1, 2, 1, 2, 1, 2]` is valid. Standard approach:
  1. Find the median. Sorting gives O(n log n). Quickselect gives O(n) on average.
  2. Put the larger half on the odd indices and the smaller half on the even indices, each filled from the largest down. This keeps copies of the median as far apart as possible.

  ```java
  public void wiggleSortII(int[] nums) {
      int[] s = nums.clone();
      java.util.Arrays.sort(s);
      int n = nums.length, hi = n - 1, lo = (n - 1) / 2;
      for (int i = 0; i < n; i++) {
          nums[i] = (i % 2 == 1) ? s[hi--] : s[lo--];
      }
  }
  ```

  O(n log n) time, O(n) space. The O(n) time and O(1) space version uses quickselect plus three-way partitioning with "virtual indexing" `(1 + 2*i) % (n | 1)`. Mention it, but the sorted version is what most interviewers expect you to write.
