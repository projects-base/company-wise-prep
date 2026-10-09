**Short answer:** The radius must cover the worst-served house, so the answer is the maximum over houses of the distance to that house's nearest heater. Sort the heaters; then find each house's nearest heater by binary search, or sort the houses too and sweep with one pointer that only moves forward. O((n + m) log(n + m)).

## Approach

- **Brute force:** for each house, scan all heaters for the nearest. O(n·m), 10⁸ for the hidden tests.
- **Key insight:** a house is covered with radius `r` iff its nearest heater is within `r`. So `answer = max over houses of min over heaters |house − heater|`. The inner `min` is a nearest-neighbour lookup, which sorting makes fast.
- **Binary search version:** sort heaters; for each house, find the insertion point and compare the heater just before and just after.
- **Two-pointer version (used below):** sort both. As houses increase, their nearest heater index never decreases, so pointer `j` only moves forward. Advance `j` while the next heater is at least as close.

## Solution

```java
import java.util.*;

class Solution {
    public int findRadius(int[] houses, int[] heaters) {
        int[] h = houses.clone(), t = heaters.clone();
        Arrays.sort(h);
        Arrays.sort(t);
        int j = 0, best = 0;
        for (int x : h) {
            // move to the heater closest to x; heaters are sorted and houses ascending
            while (j + 1 < t.length && Math.abs((long) t[j + 1] - x) <= Math.abs((long) t[j] - x)) j++;
            best = Math.max(best, Math.abs(t[j] - x));
        }
        return best;
    }
}
```

The `<=` matters: with duplicate heater positions, `<` would leave `j` stuck behind a tie and could block progress for later houses. The arrays are cloned so the caller's input is not reordered.

## Complexity

- **Time O(n log n + m log m)** for sorting; the sweep is O(n + m) because `j` only moves forward.
- **Space O(n + m)** for the copies (O(log) extra if sorting in place is allowed).

## Edge cases

- One heater: answer is the distance to the farthest house (Example 3).
- House on a heater: distance 0.
- All houses left of all heaters, or right: `j` stays at the first heater or runs to the last.
- Values up to 10⁹: differences fit in `int`, but the `long` casts make the comparison obviously safe.

## Variations

- Binary search on the radius with a greedy coverage check also works, O((n + m) log range), but is slower and more code.
- If heaters can be placed freely and you minimise their count for a fixed radius, it becomes a greedy interval-cover problem.

Practise it in the app: Run / Submit on this page.
