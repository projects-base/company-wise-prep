**Short answer:** A subarray `i..j` sums to `k` exactly when `prefix[j] - prefix[i-1] = k`, that is when an earlier prefix sum equals `prefix[j] - k`. Walk the array once, keep a hash map from prefix sum to how many times it has occurred, and at each step add the count of `sum - k`. Seed the map with `{0: 1}` for subarrays that start at index 0. O(n) time and space.

## Approach

- **Brute force.** Every start, every end, running sum: O(n²). With 40,000 elements that is 8·10⁸ additions, too slow here.
- **Why not a sliding window?** A window relies on the sum growing when you extend and shrinking when you trim. Negative numbers and zeros break that, so you cannot decide which pointer to move.
- **Key insight.** Prefix sums turn "subarray sum" into "difference of two prefix sums". For each end position you need the number of earlier prefix sums equal to `sum - k`. A hash map of counts answers that in O(1).
- **Order matters.** Look up `sum - k` *before* adding the current `sum` to the map. Otherwise, with `k = 0`, you would count the empty subarray ending here.

## Solution

```java
import java.util.*;

class Solution {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> seen = new HashMap<>(); // prefix sum -> how many times it occurred
        seen.put(0, 1);                                // empty prefix
        int sum = 0, count = 0;
        for (int x : nums) {
            sum += x;
            count += seen.getOrDefault(sum - k, 0);
            seen.merge(sum, 1, Integer::sum);
        }
        return count;
    }
}
```

Prefix sums stay within ±5·10⁷ here, so `int` is safe. With larger values use `long` keys.

## Complexity

- **Time:** O(n) expected, one pass with O(1) hash operations.
- **Space:** O(n) for the map in the worst case (all prefix sums distinct).

## Edge cases

- `k = 0`: counts zero-sum subarrays; `[0,0,0]` gives 6.
- Negative numbers: `[1,-1,1]` with `k = 1` gives 3.
- The whole array is the only match: found through the seeded `{0: 1}`.
- A single element equal to `k`.

## Variations

- **All values positive:** a sliding window works with O(1) space.
- **Longest subarray with sum k:** store the *first* index of each prefix sum instead of a count.
- **Divisible by k (LC 974):** key the map by `((sum % k) + k) % k`.
- **Binary array, count subarrays with sum k / equal 0s and 1s:** same idea, often with an array instead of a map.
- **2D version (count submatrices summing to target):** fix a pair of rows, collapse columns, run this 1D algorithm: O(rows² · cols).

Practise it in the app: Run / Submit on this page.
