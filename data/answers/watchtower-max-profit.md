**Short answer:** Profit only changes in steps. Between two house distances, a taller tower covers the same houses and just costs more. So the best height is 0, or exactly the distance of some house. Sort the distances. At the k-th distinct distance `d`, all houses up to it are covered, and profit = `pay · count − cost · d`. Take the maximum over those candidates and 0. This is O(n log n).

## Approach

- **Brute force.** Try every candidate height (each house distance), and count the houses within it: O(n²).
- **Key insight 1: only a few heights matter.** Profit as a function of `h` falls in a straight line, then jumps up when `h` reaches a house's distance. So the maximum is always at the start of a step: `h = 0` or `h = distance of some house`.
- **Key insight 2: sorting gives counts for free.** After sorting the distances, the number of houses within the i-th distance is `i + 1`, except where several houses share a distance. Group equal distances and evaluate only at the end of each group, so the count includes all of them.
- **Exactness.** Compare squared distances as `long`s. Then houses at the same distance group exactly, with no floating-point error. Take `sqrt` only to compute the cost.

## Solution

```java
import java.util.*;

class Solution {
    public double maxProfit(int[][] houses, int costPerHeight, int payPerHouse) {
        int n = houses.length;
        long[] d2 = new long[n];
        for (int i = 0; i < n; i++)
            d2[i] = (long) houses[i][0] * houses[i][0] + (long) houses[i][1] * houses[i][1];
        Arrays.sort(d2);
        double best = 0;                         // height 0, nothing paid
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n && d2[j] == d2[i]) j++; // group houses at the same distance
            double profit = (double) payPerHouse * j - (double) costPerHeight * Math.sqrt((double) d2[i]);
            best = Math.max(best, profit);
            i = j;
        }
        return best;
    }
}
```

Houses at the origin have `d2 = 0`. They form the first group and are counted at cost 0, which handles "height 0 covers houses at the origin".

## Complexity

- **Time:** O(n log n) for the sort. The scan is O(n).
- **Space:** O(n) for the distances.

## Edge cases

- Every house is too far to pay for itself: the answer is 0.
- Several houses at the same distance: they must be counted together.
- Houses at the origin.
- A large profit: 10⁵ × 10⁴ = 10⁹ fits in a `double` exactly.

## Follow-ups

- **The tower location `(px, py)` is given, and coordinates and costs may be floats.** Compute `d = hypot(x − px, y − py)` as a `double` and sort. Then evaluate `pay · (i + 1) − cost · d[i]` at **every** index. With ties, the last index of a group has the same cost and the highest count, so the maximum over all indices is still correct, and no exact grouping is needed. Floating-point noise only matters if two distances are meant to be equal but differ in the last bits. Mention an epsilon (for example 1e-9) if exact ties matter. Use `Math.hypot`, or squared distances, to avoid overflow and rounding surprises.
- **"Horizontal distance" (houses on a line):** use `|x − px|` as the distance. The algorithm is unchanged.
- **Choose the tower location too:** much harder. The optimum circle passes through houses on its boundary, which leads to O(n²) or O(n³) candidate centres. That is a geometry problem, not a sort.
- **Different payment per house:** sort by distance and keep a running sum of payments instead of a count.

Practise it in the app: Run / Submit on this page.
