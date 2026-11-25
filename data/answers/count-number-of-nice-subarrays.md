**Short answer:** Replace each number by 1 if it is odd and 0 if even; the task becomes "count subarrays with sum k". Keep the running odd count and how many prefixes had each count, and at each end add the number of prefixes with count `odd - k`, for O(n) time.

## Picture it

`nums = [2, 1, 2, 1, 2]`, `k = 2` (answer 4). `seen[c]` is the number of prefixes so far that contain `c` odd numbers.

| step | x | odd so far | look up `odd - k` | found | ans | seen after |
|---|---|---|---|---|---|---|
| start | | 0 | | | 0 | {0:1} |
| 1 | 2 | 0 | -2 | none | 0 | {0:2} |
| 2 | 1 | 1 | -1 | none | 0 | {0:2, 1:1} |
| 3 | 2 | 1 | -1 | none | 0 | {0:2, 1:2} |
| 4 | 1 | 2 | 0 | 2 | 2 | {0:2, 1:2, 2:1} |
| 5 | 2 | 2 | 0 | 2 | 4 | {0:2, 1:2, 2:2} |

- At steps 4 and 5 the two matching prefixes mean two possible starts: before or after the first `2`.
- Two starts times two ends (index 3 or 4) gives the 4 subarrays.

**The picture in one sentence:** each earlier prefix with exactly `k` fewer odds is one valid start, so a counting array turns the search into a lookup.

## Approach

- **Brute force:** try every start, extend the end and count odds. O(n²).
- **Key insight:** subarray `i..j` is nice exactly when the odd count up to `j` minus the odd count before `i` is `k`.
- **Optimal:** count prefixes by their odd total. Totals lie in `0..n`, so an `int[]` beats a hash map.
- **Why one plain window is awkward:** for a fixed end, several starts are valid because evens on the left are free. One left pointer cannot count them all by itself.

## Solution

```java
class Solution {
    public int numberOfSubarrays(int[] nums, int k) {
        int[] seen = new int[nums.length + 1];   // seen[c] = prefixes containing c odd numbers
        seen[0] = 1;                             // empty prefix
        int odd = 0, ans = 0;
        for (int x : nums) {
            odd += x & 1;                        // also right for negative odd numbers
            if (odd >= k) ans += seen[odd - k];
            seen[odd]++;
        }
        return ans;
    }
}
```

## Complexity

- **Time:** O(n), one pass with O(1) work per element.
- **Space:** O(n) for `seen`.

## Edge cases

- No odd numbers and `k >= 1`: 0.
- `k` larger than the odd count: 0, because `odd >= k` never holds.
- `k = 0` (outside LeetCode's limits): counts all-even subarrays. It works because the lookup happens before `seen[odd]++`.
- The whole array is the only match: found through the seeded `seen[0] = 1`.
- For n ≤ 50,000 there are at most about 1.25·10⁹ subarrays, which fits in `int`. Use `long` for larger n.

## Variations

- **O(1) space, sliding window:** exactly k = (at most k) − (at most k − 1). Each "at most" is a normal window.

```java
class AtMostWindow {
    int numberOfSubarrays(int[] nums, int k) {
        return atMost(nums, k) - atMost(nums, k - 1);
    }

    private int atMost(int[] nums, int k) {
        if (k < 0) return 0;
        int left = 0, odd = 0, total = 0;
        for (int right = 0; right < nums.length; right++) {
            odd += nums[right] & 1;
            while (odd > k) odd -= nums[left++] & 1;
            total += right - left + 1;           // all starts left..right are valid
        }
        return total;
    }
}
```

- **Binary Subarrays With Sum (LC 930):** the same problem with the 0/1 array given directly.
- **Subarrays with K Different Integers (LC 992):** the same "at most" subtraction, where prefix counts do not work.

Related: [Subarray Sum Equals K](subarray-sum-equals-k.md), [T1 · Hashing, two pointers, sliding window and prefix sums](../academy/lessons/T1.md).
