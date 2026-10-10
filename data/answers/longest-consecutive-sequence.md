**Short answer:** Put all numbers in a `HashSet`. A number `x` starts a run only if `x - 1` is not in the set. For each start, count upward (`x + 1`, `x + 2`, ...) while the next value exists, and keep the longest length. Every value is visited at most twice in total, so it is O(n) time and O(n) space, without sorting.

## Picture it

Example 1: `nums = [100,4,200,1,3,2]`, set = {1, 2, 3, 4, 100, 200}. The loop visits the set in its own order; only run starts do any counting.

| x | x − 1 in set? | Start of a run? | Count upward | len | best |
|---|---|---|---|---|---|
| 1 | 0: no | yes | 2 ✓, 3 ✓, 4 ✓, 5 ✗ | 4 | 4 |
| 2 | 1: yes | no, skip | — | — | 4 |
| 3 | 2: yes | no, skip | — | — | 4 |
| 100 | 99: no | yes | 101 ✗ | 1 | 4 |
| 4 | 3: yes | no, skip | — | — | 4 |
| 200 | 199: no | yes | 201 ✗ | 1 | 4 |

```text
number line:  1 2 3 4 . . . 100 . . . 200
              ^ start        ^ start   ^ start    (only starts walk right)
```

**The picture in one sentence:** only count from numbers whose left neighbour is missing, so each run is walked once from its start and the whole thing is O(n).

## Approach

- **Brute force:** for each number, count up by looking for `x + 1` with a linear scan. O(n²) or worse.
- **Sorting:** sort, then walk once, skipping duplicates and resetting the counter on a gap. O(n log n). A good first answer, but not O(n).
- **Key insight:** with a hash set, membership is O(1). The waste in the naive set version is starting a count from the middle of a run. Only start from `x` when `x - 1` is missing. Then each run is walked exactly once, from its first element.
- **Optimal:** the set with the "start of run" check.

## Solution

```java
import java.util.*;

class Solution {
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int x : nums) set.add(x);
        int best = 0;
        for (int x : set) {                 // iterate the set, not nums, to skip duplicates
            if (set.contains(x - 1)) continue;   // not the start of a run
            int len = 1;
            while (set.contains(x + len)) len++;
            best = Math.max(best, len);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n) on average. The outer loop is O(n). The inner `while` only runs from run starts, and the runs do not overlap, so all inner steps together add up to at most n.
- **Space:** O(n) for the set.

## Edge cases

- Empty array: return 0.
- Duplicates (`[1, 0, 1, 2]` gives 3): the set removes them. Iterating `nums` instead of the set can repeat work when there are many copies of a run start.
- Negative numbers and `Integer.MIN_VALUE`: `x - 1` overflows to `MAX_VALUE`, which is harmless for the check. `x + len` can overflow near `MAX_VALUE`; the stated range (±10⁹) avoids it.
- All the same value: answer 1.

## Variations

- **Contiguous variant (LeetCode 674, longest continuous increasing subarray):** a different problem. The elements must be adjacent in the array and strictly increasing, not consecutive integers. One pass: `cur = nums[i] > nums[i-1] ? cur + 1 : 1`, track the max. O(n) time, O(1) space.
- **Union-find:** union `x` with `x + 1` when both exist, and the answer is the largest component size. Also near O(n), but more code. It shows you know the technique.
- **Return the run itself:** remember the start `x` along with `best`.

Practise it in the app: Run / Submit on this page.
