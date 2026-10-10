**Short answer:** Use a monotonic deque of indices whose values decrease from front to back. For each new element, drop the front if it has left the window, pop from the back every index whose value is not larger than the new one (it can never be a maximum again), then push the new index. The front is always the window maximum. Every index is pushed and popped at most once, so it is O(n).

## Picture it

Example 1: `nums = [1,3,-1,-3,5,3,6,7]`, `k = 3`. The deque holds indices; values shown as `index:value`.

| i | nums[i] | expired from front | popped from back | deque after (front → back) | output |
|---|---|---|---|---|---|
| 0 | 1 | - | - | `0:1` | - |
| 1 | 3 | - | `0:1` | `1:3` | - |
| 2 | -1 | - | - | `1:3, 2:-1` | 3 |
| 3 | -3 | - | - | `1:3, 2:-1, 3:-3` | 3 |
| 4 | 5 | `1:3` (1 ≤ 4-3) | `3:-3, 2:-1` | `4:5` | 5 |
| 5 | 3 | - | - | `4:5, 5:3` | 5 |
| 6 | 6 | - | `5:3, 4:5` | `6:6` | 6 |
| 7 | 7 | - | `6:6` | `7:7` | 7 |

Result: `[3,3,5,5,6,7]`.

**The picture in one sentence:** a newcomer evicts every smaller value behind it (they can never win again), so the deque stays decreasing and its front is always the window max.

## Approach

- **Brute force:** scan each window: O(n·k), too slow for n = 25,000 and k = 20,000.
- **Better:** a max-heap (or `TreeMap` with counts) of the window: O(n log k).
- **Key insight:** if `nums[j] >= nums[i]` and `j > i`, then i is useless from now on: j is at least as large and stays in the window longer. So the useful candidates form a decreasing sequence, which a deque maintains.
- **Optimal:** the monotonic deque. Store indices, not values, so you can tell when the front has expired (`index <= i - k`).

## Solution

```java
class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] out = new int[n - k + 1];
        int[] dq = new int[n];          // indices; values decrease from head to tail
        int head = 0, tail = 0;
        for (int i = 0; i < n; i++) {
            if (head < tail && dq[head] <= i - k) head++;                 // expired
            while (head < tail && nums[dq[tail - 1]] <= nums[i]) tail--;  // dominated
            dq[tail++] = i;
            if (i >= k - 1) out[i - k + 1] = nums[dq[head]];
        }
        return out;
    }
}
```

An `int[]` with head/tail pointers acts as the deque; it avoids boxing compared with `ArrayDeque<Integer>`.

## Complexity

- **Time:** O(n), amortised: each index enters and leaves the deque at most once.
- **Space:** O(n) for the array-backed deque here (O(k) with a circular buffer or `ArrayDeque`), plus the output.

## Edge cases

- `k == 1`: output equals the input.
- `k == n`: a single maximum.
- Equal values: popping on `<=` keeps the newest copy, which is correct and keeps the deque short.
- All decreasing: the deque grows to k and the front expires each step.

## Follow-ups

- **O(n log k) time with O(1) extra space:** use in-place doubling on a copy you are allowed to overwrite (or the input, if mutation is allowed). After pass p, `a[i]` holds the max of the 2ᵖ elements starting at i: `a[i] = max(a[i], a[i + L])`, iterating i upward so `a[i + L]` is still the old value. Stop at L = the largest power of two ≤ k, which takes about log k passes of O(n). Then each window max is `max(a[i], a[i + k - L])`, because two overlapping blocks of length L cover the window. Only a few variables beyond the output are used.
- **Block trick (O(n) time, O(n) space):** split into blocks of size k, build prefix-max and suffix-max arrays; window max is `max(suffix[i], prefix[i + k - 1])`.

See [C5 · Amortised analysis](../academy/lessons/C5.md) for why the deque is O(n).

Practise it in the app: Run / Submit on this page.
