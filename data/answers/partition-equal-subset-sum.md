**Short answer:** Two equal halves means one subset sums to exactly `total / 2`. If the total is odd, the answer is false. Otherwise it is a 0/1 knapsack: a boolean array `can[s]` says whether some subset reaches sum `s`, filled one number at a time. That runs in O(n · total) time and O(total) space.

## Picture it

Example 1: `nums = [1,5,11,5]`, total 22, `half = 11`. `T` = `can[s]` is true after processing that number.

| After x | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| start | T | | | | | | | | | | | |
| 1 | T | T | | | | | | | | | | |
| 5 | T | T | | | | T | T | | | | | |
| 11 | T | T | | | | T | T | | | | | T |
| 5 | T | T | | | | T | T | | | | T | T |

Each `can[s]` depends on the previous row's `can[s]` and `can[s − x]`. Going right to left (s from 11 down to x) means `can[s − x]` is still the previous row's value, so each number is used at most once. `can[11]` is true: {11} and {1, 5, 5}.

**The picture in one sentence:** track which sums are reachable, not which subsets, and scan the sums downwards so each number is added only once.

## Approach

- **Brute force.** Try every subset and check whether one sums to half. That is 2ⁿ subsets, hopeless for n = 200.
- **Key insight.** We never care *which* elements are used, only *which sums are reachable*. With values ≤ 100 and n ≤ 200, the total is at most 20,000, so the set of reachable sums is small.
- **Optimal.** Keep `can[0..half]`, with `can[0] = true`. For each number `x`, every sum `s` that was reachable before becomes `s + x` reachable. Loop `s` **downwards** from `half` to `x` so that `x` is used at most once (going upwards would reuse the same `x` repeatedly, which is the unbounded knapsack).

## Solution

```java
class Solution {
    public boolean canPartition(int[] nums) {
        int total = 0;
        for (int x : nums) total += x;
        if (total % 2 != 0) return false;
        int half = total / 2;
        boolean[] can = new boolean[half + 1]; // can[s]: some subset sums to s
        can[0] = true;
        for (int x : nums) {
            for (int s = half; s >= x; s--) {
                if (can[s - x]) can[s] = true;
            }
        }
        return can[half];
    }
}
```

You can return early once `can[half]` becomes true, and you can return false early if any single element is larger than `half`.

## Complexity

- **Time:** O(n · half), at most 200 × 10,000 = 2 million cheap steps.
- **Space:** O(half) for the one-dimensional table. The 2D table `dp[i][s]` works too but is not needed, because row `i` only reads row `i - 1`.

## Edge cases

- Odd total: false immediately.
- A single element: the total is that element, and an empty other half cannot match it (values are positive), so false.
- One element equal to `half`: true (that element alone is one side).
- All equal values with even count: true.

## Variations

- **Bitset trick:** `BitSet` or `long[]` shifting (`bits |= bits << x`) does the same work about 64 times faster.
- **Target sum / minimum subset-difference:** same reachable-sums table; pick the reachable `s ≤ total/2` closest to `total/2`.
- **Partition into k equal subsets:** needs backtracking or bitmask DP; the knapsack table is no longer enough.

See [C1 · From constraints to the expected complexity](../academy/lessons/C1.md) for why n ≤ 200 with values ≤ 100 points to a pseudo-polynomial DP.

Practise it in the app: Run / Submit on this page.
