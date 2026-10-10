**Short answer:** Once you take one `x`, you should take every copy of `x`, so collapse the array into `gain[v] = v × count(v)`. Taking value v forbids v − 1 and v + 1, which is exactly House Robber over the values 0..max: adjacent "houses" cannot both be robbed. Run the take/skip DP over the values in O(n + max) time.

## Picture it

`nums = [2,2,3,3,3,4]` collapses to `gain = [0, 0, 4, 9, 4]` for values 0..4. Then the House Robber sweep:

| v | gain[v] | `take` = previous `skip` + gain[v] | `skip` = max(previous `take`, previous `skip`) |
|---|---|---|---|
| start | – | 0 | 0 |
| 0 | 0 | 0 + 0 = 0 | max(0, 0) = 0 |
| 1 | 0 | 0 + 0 = 0 | max(0, 0) = 0 |
| 2 | 4 | 0 + 4 = 4 | max(0, 0) = 0 |
| 3 | 9 | 0 + 9 = 9 | max(4, 0) = 4 |
| 4 | 4 | 4 + 4 = 8 | max(9, 4) = 9 |

Answer `max(8, 9) = 9`: take all the 3s, skip 2 and 4. Each row depends only on the row above. `take` must come from the previous `skip` because v − 1 and v cannot both be taken.

**The picture in one sentence:** bucket equal values into one "house" worth `v × count`, and neighbouring values become neighbouring houses in House Robber.

## Approach

**Brute force.** Try every order of picks recursively. Exponential, and 20,000 numbers rule it out.

**Key insight 1.** Taking a copy of x deletes all x − 1 and x + 1, but never other copies of x. So after the first x, the rest of the x's are free points: you either take **all** copies of a value or **none**. That turns the problem into choosing a set of values, with total `gain[v] = v · count(v)`.

**Key insight 2.** The only constraint is "not both v and v + 1". Lay the values out on a number line 0..max and this is House Robber: maximise the sum of chosen positions with no two adjacent.

**Optimal.** Bucket the gains, then sweep v upward keeping two numbers:

- `take` = best total where v is taken (so v − 1 was skipped): `skip_prev + gain[v]`;
- `skip` = best total where v is not taken: `max(take_prev, skip_prev)`.

## Solution

```java
import java.util.*;

class Solution {
    public int deleteAndEarn(int[] nums) {
        int max = 0;
        for (int x : nums) max = Math.max(max, x);
        int[] gain = new int[max + 1]; // total points from taking every copy of v
        for (int x : nums) gain[x] += x;
        // house robber over values: can't take both v and v - 1
        int take = 0, skip = 0;
        for (int v = 0; v <= max; v++) {
            int t = skip + gain[v];
            skip = Math.max(skip, take);
            take = t;
        }
        return Math.max(take, skip);
    }
}
```

Trace `[2,2,3,3,3,4]`: gain[2] = 4, gain[3] = 9, gain[4] = 4. Taking 3 (9) beats taking 2 and 4 (8), so the answer is 9.

## Complexity

- **Time:** O(n + M), where M = max value ≤ 10⁴.
- **Space:** O(M) for the gain array.

## Edge cases

- Single element → that element.
- Values with gaps (e.g. 2 and 5): gain is 0 in between, so the DP lets you take both, which is correct since they do not conflict.
- Overflow: the maximum total is 2·10⁴ × 10⁴ = 2·10⁸, which fits in `int`.

## Variations

- **Huge value range (say up to 10⁹):** sort the distinct values instead of bucketing. Walk them in order; if the current value is exactly previous + 1, apply the House Robber rule, otherwise there is no conflict and you can add its gain to the best so far. O(n log n).
- **House Robber II (circular), House Robber III (tree):** same take/skip idea on different shapes.

See also [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
