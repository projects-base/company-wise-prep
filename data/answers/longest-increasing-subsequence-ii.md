**Short answer:** Let `best[v]` be the longest valid subsequence ending with value v. Scanning left to right, the answer for the current value v is `1 + max(best[v − k .. v − 1])`. Keep `best` in a segment tree indexed by value, so that range-max query and point update are both O(log V). For the "difference exactly 1" version you do not need a tree at all: `best[v] = best[v − 1] + 1` with a hash map, O(n).

## Picture it

Example 1: `nums = [4,2,1,4,3,4,5,8,15]`, `k = 3`. The segment tree holds `best[value]`; each step is one range-max query over the previous `k` values and one point update.

| i | v | Query range [v−k, v−1] | max found | len | best[] after (non-zero) |
|---|---|---|---|---|---|
| 0 | 4 | [1, 3] | 0 | 1 | 4:1 |
| 1 | 2 | [0, 1] | 0 | 1 | 2:1, 4:1 |
| 2 | 1 | [0, 0] | 0 | 1 | 1:1, 2:1, 4:1 |
| 3 | 4 | [1, 3] | 1 (from 1 or 2) | 2 | 1:1, 2:1, 4:2 |
| 4 | 3 | [0, 2] | 1 | 2 | 1:1, 2:1, 3:2, 4:2 |
| 5 | 4 | [1, 3] | 2 (from 3) | 3 | ..., 3:2, 4:3 |
| 6 | 5 | [2, 4] | 3 (from 4) | 4 | ..., 4:3, 5:4 |
| 7 | 8 | [5, 7] | 4 (from 5) | 5 | ..., 5:4, 8:5 |
| 8 | 15 | [12, 14] | 0 | 1 | ..., 8:5, 15:1 |

Answer: 5, the chain 1 → 3 → 4 → 5 → 8. Each cell depends only on cells to its left in value (`v−k .. v−1`) written at earlier positions.

**The picture in one sentence:** index the DP by value instead of position, so "best among earlier values within k" becomes one range-max query on a segment tree.

## Approach

- **Brute force:** the classic LIS DP, for each i look at every j < i with `nums[i] − k ≤ nums[j] < nums[i]`. O(n²) = 10¹⁰ at the limits. Too slow.
- **Key insight:** the DP only asks "what is the best length among earlier elements whose *value* lies in a window?" Index the state by value instead of position. Processing left to right guarantees that everything in the structure came earlier in the array.
- **Optimal:** a max segment tree over values 0..max(nums). For each v: query `[v − k, v − 1]`, add 1, update position v with the larger of old and new. Strictly increasing is guaranteed because the query range stops at v − 1.
- **Why not the patience-sorting LIS trick (binary search on tails)?** It cannot respect the "difference at most k" rule.

## Solution

```java
class Solution {
    private int size;
    private int[] tree;   // tree[size + v] = best length ending in value v

    public int lengthOfLIS(int[] nums, int k) {
        int maxV = 0;
        for (int x : nums) maxV = Math.max(maxV, x);
        size = 1;
        while (size <= maxV) size <<= 1;
        tree = new int[2 * size];
        int best = 0;
        for (int v : nums) {
            int len = query(Math.max(0, v - k), v - 1) + 1;
            update(v, len);
            best = Math.max(best, len);
        }
        return best;
    }

    private void update(int pos, int val) {
        int i = pos + size;
        if (tree[i] >= val) return;
        tree[i] = val;
        for (i >>= 1; i >= 1; i >>= 1) tree[i] = Math.max(tree[2 * i], tree[2 * i + 1]);
    }

    // max over values in [lo, hi], iterative bottom-up segment tree
    private int query(int lo, int hi) {
        if (lo > hi) return 0;
        int res = 0, l = lo + size, r = hi + size + 1;
        while (l < r) {
            if ((l & 1) == 1) res = Math.max(res, tree[l++]);
            if ((r & 1) == 1) res = Math.max(res, tree[--r]);
            l >>= 1;
            r >>= 1;
        }
        return res;
    }
}
```

## The "difference exactly 1" version (the original prompt)

If neighbours must differ by exactly 1, the range shrinks to a single value, so a map is enough:

```java
import java.util.HashMap;
import java.util.Map;

class Solution {
    public int longestConsecutiveInOrder(int[] nums) {
        Map<Integer, Integer> best = new HashMap<>();
        int ans = 0;
        for (int v : nums) {
            int len = best.getOrDefault(v - 1, 0) + 1;
            best.merge(v, len, Math::max);
            ans = Math.max(ans, len);
        }
        return ans;
    }
}
```

O(n) time and space. This is exactly the k = 1 case of the main solution.

## Complexity

- **Time:** O(n log V), V = max value (10⁵, so about 17 levels).
- **Space:** O(V) for the tree. If values were up to 10⁹, coordinate-compress them first (sort distinct values, binary search the range bounds), giving O(n log n) time and O(n) space.

## Edge cases

- Equal values: not allowed next to each other (strict), and the query excludes v itself.
- `v − k < 0`: clamp the range start to 0.
- `v = 1`: the range is empty (`lo > hi` or only 0), length 1.
- Repeated value later in the array: keep the max with what is already stored, never overwrite downwards.

## Follow-up: difference at most d

That is this solution with k = d. If d is huge relative to V, the query covers everything below v and this becomes a plain LIS solved in O(n log V). A Fenwick tree works for prefix max but not for an arbitrary window `[v − d, v − 1]`, which is why a segment tree is the natural choice here.

Practise it in the app: Run / Submit on this page.
