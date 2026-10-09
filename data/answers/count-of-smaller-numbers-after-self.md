**Short answer:** Walk the array from right to left and keep a Fenwick tree (binary indexed tree) counting how many times each value has been seen. For `nums[i]`, the answer is the prefix count of values strictly below `nums[i]`; then add `nums[i]` to the tree. Values lie in −10⁴..10⁴, so shift them to 1..20001 and index the tree directly. O(n log V).

## Approach

**Brute force.** For each i, scan every j > i and count `nums[j] < nums[i]`. O(n²), too slow for 10⁵.

**Key insight.** Processing right to left turns "elements to my right" into "elements already processed". The question becomes: among the values inserted so far, how many are less than x? That is a prefix sum over a frequency array with point updates, which a Fenwick tree answers in O(log V) each.

**Optimal.**

1. Map each value to an index: `v = nums[i] + 10001` (so −10⁴ becomes 1).
2. For i from n−1 down to 0: query the prefix sum up to `v − 1` (strictly smaller), store it, then add 1 at `v`.

If values were not bounded, compress them first: sort the distinct values and use each value's rank as its index.

## Solution

```java
import java.util.*;

class Solution {
    private static final int OFFSET = 10_001; // maps -10^4..10^4 to 1..20001

    // Walk from the right, keeping a Fenwick tree of how many times each value has been seen.
    public List<Integer> countSmaller(int[] nums) {
        int size = 2 * OFFSET + 1;
        int[] tree = new int[size + 1];
        Integer[] out = new Integer[nums.length];
        for (int i = nums.length - 1; i >= 0; i--) {
            int v = nums[i] + OFFSET;
            int smaller = 0;
            for (int k = v - 1; k > 0; k -= k & -k) smaller += tree[k]; // count of values in [1, v-1]
            out[i] = smaller;
            for (int k = v; k <= size; k += k & -k) tree[k]++;          // record this value
        }
        return Arrays.asList(out);
    }
}
```

`k & -k` isolates the lowest set bit. Subtracting it walks down through the blocks that make up a prefix; adding it walks up through every block that contains index k.

## Complexity

- **Time:** O(n log V) with V ≈ 20,001, about 15 steps per query or update.
- **Space:** O(V) for the tree plus O(n) for the output.

## Edge cases

- Duplicates: the query stops at `v − 1`, so equal values are not counted; `[-1,-1]` gives `[0,0]`.
- Single element → `[0]`.
- Fenwick trees are 1-indexed; index 0 must never hold data. That is why the shift is 10001, not 10000.

## Variations

- **Merge sort with counting:** sort indices by value; while merging, when an element from the left half is placed, add the number of right-half elements already placed before it. O(n log n), no value bound needed.
- **Segment tree or order-statistic tree:** same idea, more code.
- **Inversion count, greater-before-self:** the same Fenwick pattern with a different direction or query.
- **Count of Range Sum (LeetCode 327), Reverse Pairs (LeetCode 493):** merge-sort counting relatives.

See also [C1 · From constraints to the expected complexity](../academy/lessons/C1.md).

Practise it in the app: Run / Submit on this page.
