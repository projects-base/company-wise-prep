**Short answer:** A value is always found exactly when it is greater than every element to its left and smaller than every element to its right. Compute a running maximum from the left and a running minimum from the right, and count the positions that pass both checks. That is two linear passes, O(n) time.

## Approach

**Brute force, O(n²).** For each element, scan everything to its left and to its right and check the condition above. Correct, but slow for 10⁵ elements.

**Why the condition is right.** Say we search for `t` at index `i`:

- If some element `x` to the left of `t` is larger than `t`, picking `x` as pivot drops `x` and everything to its right, which includes `t`. So `t` can be lost.
- If some element `y` to the right of `t` is smaller, picking `y` drops `y` and everything to its left, which includes `t`.
- If neither exists, every pivot left of `t` is smaller (the search keeps the right side, so `t` survives) and every pivot right of `t` is larger (the search keeps the left side, so `t` survives). The sequence shrinks until `t` itself is picked.

**Better.** Precompute `prefixMax[i]` and `suffixMin[i]` arrays, then test each index. O(n) time, two extra arrays.

**Optimal.** One pass left to right records whether `nums[i]` beats the running maximum. A second pass right to left keeps a running minimum and counts the indices that pass both tests. Only one boolean array is needed.

## Solution

```java
import java.util.*;

class Solution {
    // A value is always found exactly when it is larger than everything to its left
    // and smaller than everything to its right.
    public int binarySearchableNumbers(int[] nums) {
        int n = nums.length;
        boolean[] okLeft = new boolean[n];
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            okLeft[i] = nums[i] > max;
            max = Math.max(max, nums[i]);
        }
        int min = Integer.MAX_VALUE, count = 0;
        for (int i = n - 1; i >= 0; i--) {
            if (okLeft[i] && nums[i] < min) count++;
            min = Math.min(min, nums[i]);
        }
        return count;
    }
}
```

## Complexity

- **Time:** O(n). Two passes, constant work per element.
- **Space:** O(n) for the `okLeft` flags. The left-side fact and the right-side fact are learned in different passes, so one of them must be stored.

## Edge cases

- A single element is always found (answer 1).
- A sorted array: every element counts (answer n).
- A strictly decreasing array of length 2 or more: answer 0.
- The sentinels `Integer.MIN_VALUE` / `MAX_VALUE` are safe because values are within ±10⁵. For arbitrary ints, use a "nothing seen yet" flag instead.

## Follow-ups

- **What if duplicates are allowed?** With this pseudocode, a pivot equal to the target returns true at once, so an equal value never throws the target away. The test becomes non-strict: greater than or equal to everything on the left, and less than or equal to everything on the right. Still O(n). Say whether you count positions or distinct values, since duplicates make those differ.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for walking from O(n²) to O(n) out loud.

Practise it in the app: Run / Submit on this page.
