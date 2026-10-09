**Short answer:** Walk the array once with a hash map from value to index. For each `nums[i]`, check whether `target - nums[i]` is already in the map. If it is, return both indices. If not, store `nums[i]`. That is O(n) time and O(n) space. The brute force is O(n²). Sorting plus two pointers is O(n log n), but it needs the original indices kept alongside the values.

## Approach

- **Brute force.** Check every pair `i < j`. O(n²): 5·10⁹ pairs for 10⁵ numbers, too slow.
- **Sort + two pointers.** Sort `(value, index)` pairs. Start `l` at the smallest value and `r` at the largest. If the sum is too small, move `l` right. If it is too big, move `r` left. O(n log n) time, O(n) space for the pairs. This version is the right choice when the input is already sorted (then it needs only O(1) space), or when memory for a hash map is a concern.
- **Key insight (hash map).** For each element, the partner it needs is fully determined: `target - x`. A hash map answers "have I seen it?" in O(1). Check the map *before* inserting the current element. That way an element never pairs with itself, and duplicates such as `[3,3]` still work, because the first 3 is already in the map when the second arrives.

## Solution

```java
import java.util.*;

class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>(); // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[] {j, i};
            seen.put(nums[i], i);
        }
        return new int[0]; // unreachable when exactly one answer exists
    }
}
```

Sort + two pointers, for comparison:

```java
import java.util.*;

class SolutionSorted {
    public int[] twoSum(int[] nums, int target) {
        Integer[] idx = new Integer[nums.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingInt(i -> nums[i]));
        int l = 0, r = idx.length - 1;
        while (l < r) {
            long sum = (long) nums[idx[l]] + nums[idx[r]];
            if (sum == target) return new int[] {idx[l], idx[r]};
            if (sum < target) l++; else r--;
        }
        return new int[0];
    }
}
```

## Complexity

- **Hash map:** O(n) expected time, O(n) space.
- **Sort + two pointers:** O(n log n) time, O(n) space for the index array.

## Edge cases

- Duplicates that form the answer (`[3,3]`, target 6).
- Negative numbers and zero.
- The same element must not be used twice (`[3,2,4]`, target 6 must not return `[0,0]`).
- No answer: return an empty array or throw `IllegalArgumentException`. Agree which one with the interviewer.

## Follow-ups

- **Integer overflow.** Values and target are each at most 10⁹ in absolute value. So `target - nums[i]` can reach ±2·10⁹. That is just inside the `int` range (`Integer.MAX_VALUE` is about 2.147·10⁹), so the code above is safe here. With the full `int` range, though, `target - nums[i]` can overflow and wrap around to a wrong key. The fix is to compute in `long` (`(long) target - nums[i]`) and skip the lookup when the result is outside the `int` range. In the two-pointer version, compute the sum as `long`, as shown above.
- **Test cases to name:** the basic example; the answer at the two ends of the array; duplicates; negatives; the case where using the same element twice would wrongly succeed (`[3,2,4]`, 6); a large input for performance; values near `Integer.MAX_VALUE` and `MIN_VALUE` for overflow.
- **Return all pairs, or count pairs:** keep counts in the map; with sorting, skip duplicate values.
- **3Sum / 4Sum:** sort, fix one element, run two pointers on the rest.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for walking from brute force to optimal out loud.

Practise it in the app: Run / Submit on this page.
