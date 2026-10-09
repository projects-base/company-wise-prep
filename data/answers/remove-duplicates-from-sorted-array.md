**Short answer:** Use two pointers. `k` is the length of the deduplicated prefix. Scan with `i`; whenever `nums[i]` differs from the last kept value `nums[k-1]`, copy it to `nums[k]` and advance `k`. Sorted input means duplicates are adjacent, so one comparison is enough. O(n) time, O(1) space.

## Approach

- **Brute force.** Copy distinct values into a `LinkedHashSet` or a new array, then copy back. O(n) time but O(n) extra space, which the question forbids.
- **Key insight.** Because the array is sorted, a value is a duplicate exactly when it equals the last value you kept. The write pointer `k` never overtakes the read pointer `i`, so writing in place never destroys an unread value.
- **Optimal.** Read pointer `i`, write pointer `k`. Keep `nums[i]` when `k == 0` or `nums[i] != nums[k - 1]`.

## Solution

```java
class Solution {
    public int removeDuplicates(int[] nums) {
        int k = 0;
        for (int i = 0; i < nums.length; i++) {
            if (k == 0 || nums[i] != nums[k - 1]) nums[k++] = nums[i];
        }
        return k;
    }
}
```

## Complexity

- **Time:** O(n), one pass.
- **Space:** O(1), two indices.

## Edge cases

- One element: returns 1.
- All equal: returns 1.
- All distinct: returns n, and every write copies a value onto itself.
- Negative values: no special handling; comparison is all that matters.

## Follow-ups

- **Keep each value at most `m` times (LC 80 is `m = 2`), O(n) time and O(1) space.** Generalise the comparison: keep `nums[i]` if `k < m` or `nums[i] != nums[k - m]`. Since the array is sorted, if `nums[k - m]` equals `nums[i]`, then the last `m` kept values are all equal to it, so one more copy would exceed the limit.

```java
class Solution {
    public int removeDuplicates(int[] nums, int m) {
        int k = 0;
        for (int x : nums) {
            if (k < m || x != nums[k - m]) nums[k++] = x;
        }
        return k;
    }
}
```

- **Unsorted input:** order-preserving dedup needs a `HashSet` (O(n) space), or sort first and lose the original order.
- **Why compare with the written prefix, not with `nums[i - 1]`?** For `m = 1` both work. For general `m`, comparing with the input side gives wrong answers, because the input values at `i - m` are not the ones that were kept.

Practise it in the app: Run / Submit on this page.
