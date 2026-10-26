**Short answer:** Walk the array once with a hash map from value to index. For each `nums[i]`, check whether `target - nums[i]` is already in the map. If it is, return both indices. If not, store `nums[i]`. That is O(n) time and O(n) space. The brute force is O(n²). Sorting plus two pointers is O(n log n), but it needs the original indices kept alongside the values.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Two Sum: one pass with a hash map","code":["int[] twoSum(int[] nums, int target) {","  Map<Integer, Integer> seen = new HashMap<>();   // value -> index","  for (int i = 0; i < nums.length; i++) {","    int need = target - nums[i];","    if (seen.containsKey(need))","      return new int[]{seen.get(need), i};","    seen.put(nums[i], i);","  }","  return new int[0];","}"],"frames":[
{"note":"Goal: two numbers that add up to 10. One pass, remembering every value we have seen.","line":2,"array":{"label":"nums","values":[3,8,4,11,6]},"map":{"label":"seen (value → index)","entries":[]},"vars":{"target":10}},
{"note":"i = 0: nums[i] = 3, so we need 10 − 3 = 7. Is 7 in the map?","line":4,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":0},"marks":{"0":"cur"}},"map":{"label":"seen (value → index)","entries":[]},"vars":{"target":10,"i":0,"need":7}},
{"note":"No. Remember 3 → 0 so a later number can pair with it.","line":7,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":0},"marks":{"0":"done"}},"map":{"label":"seen (value → index)","entries":[["3",0]],"hit":"3"},"vars":{"target":10,"i":0,"need":7}},
{"note":"i = 1: nums[i] = 8, so we need 10 − 8 = 2. Is 2 in the map?","line":4,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":1},"marks":{"1":"cur"}},"map":{"label":"seen (value → index)","entries":[["3",0]]},"vars":{"target":10,"i":1,"need":2}},
{"note":"No. Remember 8 → 1 so a later number can pair with it.","line":7,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":1},"marks":{"1":"done"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1]],"hit":"8"},"vars":{"target":10,"i":1,"need":2}},
{"note":"i = 2: nums[i] = 4, so we need 10 − 4 = 6. Is 6 in the map?","line":4,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":2},"marks":{"2":"cur"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1]]},"vars":{"target":10,"i":2,"need":6}},
{"note":"No. Remember 4 → 2 so a later number can pair with it.","line":7,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":2},"marks":{"2":"done"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1],["4",2]],"hit":"4"},"vars":{"target":10,"i":2,"need":6}},
{"note":"i = 3: nums[i] = 11, so we need 10 − 11 = -1. Is -1 in the map?","line":4,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":3},"marks":{"3":"cur"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1],["4",2]]},"vars":{"target":10,"i":3,"need":-1}},
{"note":"No. Remember 11 → 3 so a later number can pair with it.","line":7,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":3},"marks":{"3":"done"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1],["4",2],["11",3]],"hit":"11"},"vars":{"target":10,"i":3,"need":-1}},
{"note":"i = 4: nums[i] = 6, so we need 10 − 6 = 4. Is 4 in the map?","line":4,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":4},"marks":{"4":"cur"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1],["4",2],["11",3]]},"vars":{"target":10,"i":4,"need":4}},
{"note":"Yes: 4 was at index 2. Answer [2, 4] in O(n), one pass, no nested loop.","line":6,"array":{"label":"nums","values":[3,8,4,11,6],"pointers":{"i":4},"marks":{"2":"hit","4":"hit"}},"map":{"label":"seen (value → index)","entries":[["3",0],["8",1],["4",2],["11",3]],"hit":"4"},"vars":{"target":10,"i":4,"need":4}}
]}
```

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
