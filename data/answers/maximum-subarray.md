**Short answer:** Kadane's algorithm. Walk the array keeping `cur`, the best sum of a block that ends at the current index. At each element, either extend the previous block or start fresh: `cur = max(x, cur + x)`. The answer is the largest `cur` seen. O(n) time, O(1) space.

## Picture it

Example 1: `nums = [-2,1,-3,4,-1,2,1,-5,4]`. `cur` = best block ending here.

| i | nums[i] | cur + nums[i] | Choice | cur | best |
|---|---|---|---|---|---|
| 0 | −2 | — | start | −2 | −2 |
| 1 | 1 | −1 | start fresh | 1 | 1 |
| 2 | −3 | −2 | extend | −2 | 1 |
| 3 | 4 | 2 | start fresh | 4 | 4 |
| 4 | −1 | 3 | extend | 3 | 4 |
| 5 | 2 | 5 | extend | 5 | 5 |
| 6 | 1 | 6 | extend | 6 | 6 |
| 7 | −5 | 1 | extend | 1 | 6 |
| 8 | 4 | 5 | extend | 5 | 6 |

Answer 6, the block [4,−1,2,1] (indices 3–6).

**The picture in one sentence:** a negative running sum only drags the next block down, so drop it and start fresh at the current element.

## Approach

- **Brute force:** every (i, j) pair with a running sum: O(n²).
- **Key insight:** the best block ending at index i is either just `nums[i]` or the best block ending at i − 1 extended by `nums[i]`. If the previous best is negative, it only hurts, so drop it. That is a one-variable DP.
- **Prefix-sum view:** the sum of a block is `prefix[j] − prefix[i]`. For each j, subtract the smallest prefix seen so far. Same O(n), and it is the bridge to the hashmap follow-up.

## Solution

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int best = nums[0], cur = nums[0];
        for (int i = 1; i < nums.length; i++) {
            cur = Math.max(nums[i], cur + nums[i]);  // extend, or start fresh here
            best = Math.max(best, cur);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n), one pass.
- **Space:** O(1).

## Edge cases

- All negative: the answer is the largest single element. Starting `best` at 0 would wrongly return 0 for a non-empty block.
- Single element: itself.
- Sums: at most 10⁵ × 10⁴ = 10⁹, which fits in `int`, but only just; use `long` if the bounds are larger.

## Follow-up: the hashmap-based optimisation

The prompt does not say exactly which hashmap follow-up was asked. The usual one moves from "maximum sum" to a *target* sum, where Kadane no longer works but prefix sums with a `HashMap` do:

- **Count subarrays with sum exactly k (LeetCode 560):** keep `count[prefix]` in a map. At each index, add `count[prefix − k]` to the answer, then record the current prefix. Start with `count[0] = 1`. O(n).

```java
import java.util.HashMap;
import java.util.Map;

class Solution {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> count = new HashMap<>();
        count.put(0, 1);
        int prefix = 0, ans = 0;
        for (int x : nums) {
            prefix += x;
            ans += count.getOrDefault(prefix - k, 0);
            count.merge(prefix, 1, Integer::sum);
        }
        return ans;
    }
}
```

- **Longest subarray with sum k:** map each prefix to its *first* index; length is `i − first[prefix − k]`.
- **Maximum sum subarray of length ≥ L, or with sum ≤ k:** prefix sums plus a deque or a `TreeSet` (`ceiling(prefix − k)`), O(n log n).

## Variations

- **Return the indexes:** remember where the current block started when you "start fresh".
- **Maximum Product Subarray:** track both the max and the min ending here, since a negative flips them.
- **Circular array:** max of plain Kadane and `total − minimum subarray` (unless all values are negative).
- **Divide and conquer:** O(n log n), sometimes asked as a follow-up for practice.

Practise it in the app: Run / Submit on this page.
