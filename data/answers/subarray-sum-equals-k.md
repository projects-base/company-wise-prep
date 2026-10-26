**Short answer:** A subarray `i..j` sums to `k` exactly when `prefix[j] - prefix[i-1] = k`, that is when an earlier prefix sum equals `prefix[j] - k`. Walk the array once, keep a hash map from prefix sum to how many times it has occurred, and at each step add the count of `sum - k`. Seed the map with `{0: 1}` for subarrays that start at index 0. O(n) time and space.

## Picture it

`nums = [1, -1, 1]`, `k = 1` (answer 3: `[1]`, `[1,-1,1]`, `[1]`). The map is shown *before* the current sum is added.

| step | x | sum | look up `sum - k` | found | count | map after adding `sum` |
|---|---|---|---|---|---|---|
| start | | 0 | | | 0 | {0:1} |
| 1 | 1 | 1 | 0 | 1 (`[1]`) | 1 | {0:1, 1:1} |
| 2 | -1 | 0 | -1 | 0 | 1 | {0:2, 1:1} |
| 3 | 1 | 1 | 0 | 2 (`[1,-1,1]` and the last `[1]`) | 3 | {0:2, 1:2} |

At step 3, prefix 0 occurred twice (the empty prefix and after `1,-1`), so two subarrays end here with sum 1.

**The picture in one sentence:** each earlier prefix equal to `sum - k` marks one start point of a subarray summing to `k`, so a count map turns the search into a lookup.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Subarray sum equals K: prefix sums + hash map","code":["int subarraySum(int[] nums, int k) {","  Map<Integer, Integer> count = new HashMap<>();   // prefix sum -> times seen","  count.put(0, 1);","  int sum = 0, ans = 0;","  for (int x : nums) {","    sum += x;","    ans += count.getOrDefault(sum - k, 0);","    count.merge(sum, 1, Integer::sum);","  }","  return ans;","}"],"frames":[
{"note":"A subarray (i, j] sums to k when prefix[j] − prefix[i] = 3. Seed the map with prefix 0 seen once.","line":3,"array":{"label":"nums","values":[1,2,1,-1,3,1]},"map":{"label":"prefix sum → count","entries":[["0",1]]},"vars":{"k":3,"sum":0,"ans":0}},
{"note":"sum = 1. Need an earlier prefix of 1 − 3 = -2: seen 0 time(s), so ans += 0.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":0},"range":[0,0]},"map":{"label":"prefix sum → count","entries":[["0",1]],"hit":null},"vars":{"k":3,"sum":1,"ans":0}},
{"note":"Record prefix 1 (now seen 1 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":0},"range":[0,0]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1]],"hit":"1"},"vars":{"k":3,"sum":1,"ans":0}},
{"note":"sum = 3. Need an earlier prefix of 3 − 3 = 0: seen 1 time(s), so ans += 1.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":1},"range":[0,1]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1]],"hit":"0"},"vars":{"k":3,"sum":3,"ans":1}},
{"note":"Record prefix 3 (now seen 1 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":1},"range":[0,1]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",1]],"hit":"3"},"vars":{"k":3,"sum":3,"ans":1}},
{"note":"sum = 4. Need an earlier prefix of 4 − 3 = 1: seen 1 time(s), so ans += 1.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":2},"range":[0,2]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",1]],"hit":"1"},"vars":{"k":3,"sum":4,"ans":2}},
{"note":"Record prefix 4 (now seen 1 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":2},"range":[0,2]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",1],["4",1]],"hit":"4"},"vars":{"k":3,"sum":4,"ans":2}},
{"note":"sum = 3. Need an earlier prefix of 3 − 3 = 0: seen 1 time(s), so ans += 1.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":3},"range":[0,3]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",1],["4",1]],"hit":"0"},"vars":{"k":3,"sum":3,"ans":3}},
{"note":"Record prefix 3 (now seen 2 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":3},"range":[0,3]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1]],"hit":"3"},"vars":{"k":3,"sum":3,"ans":3}},
{"note":"sum = 6. Need an earlier prefix of 6 − 3 = 3: seen 2 time(s), so ans += 2.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":4},"range":[0,4]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1]],"hit":"3"},"vars":{"k":3,"sum":6,"ans":5}},
{"note":"Record prefix 6 (now seen 1 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":4},"range":[0,4]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1],["6",1]],"hit":"6"},"vars":{"k":3,"sum":6,"ans":5}},
{"note":"sum = 7. Need an earlier prefix of 7 − 3 = 4: seen 1 time(s), so ans += 1.","line":7,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":5},"range":[0,5]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1],["6",1]],"hit":"4"},"vars":{"k":3,"sum":7,"ans":6}},
{"note":"Record prefix 7 (now seen 1 time(s)).","line":8,"array":{"label":"nums","values":[1,2,1,-1,3,1],"pointers":{"j":5},"range":[0,5]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1],["6",1],["7",1]],"hit":"7"},"vars":{"k":3,"sum":7,"ans":6}},
{"note":"6 subarrays sum to 3. One pass, O(n), and it works with negative numbers (a sliding window would not).","line":10,"array":{"label":"nums","values":[1,2,1,-1,3,1]},"map":{"label":"prefix sum → count","entries":[["0",1],["1",1],["3",2],["4",1],["6",1],["7",1]]},"vars":{"ans":6}}
]}
```

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
