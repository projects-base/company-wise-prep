**Short answer:** Sort the meetings by start time. Then a clash can only happen between neighbours: if any meeting starts before the previous one ends, return `false`. Otherwise `true`. O(n log n) for the sort, then one pass.

## Approach

- **Brute force:** compare every pair of meetings for overlap. O(n²).
- **Key insight:** after sorting by start, if meeting i overlaps any earlier meeting, it overlaps meeting i − 1 or i − 1 already overlapped something earlier. So checking adjacent pairs is enough. (Strictly: if no adjacent pair overlaps, each meeting ends before the next starts, so the whole sequence is disjoint.)
- **Half-open intervals:** `[start, end)`, so a meeting starting exactly when the previous one ends is fine: the check is `start < previousEnd`, not `≤`.

## Solution

```java
import java.util.Arrays;

class Solution {
    public boolean canAttendMeetings(int[][] intervals) {
        int[][] a = intervals.clone();                 // do not reorder the caller's array
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        for (int i = 1; i < a.length; i++) {
            if (a[i][0] < a[i - 1][1]) return false;   // starts before the previous one ends
        }
        return true;
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort; the scan is O(n).
- **Space:** O(n) for the copy (`clone` on `int[][]` is shallow, which is fine since we only reorder the rows). Sorting in place would be O(log n).

## Edge cases

- Empty list or one meeting: `true`.
- Back-to-back meetings (`[1,5]`, `[5,10]`): `true`.
- A meeting contained inside another (`[0,30]`, `[5,10]`): caught by the adjacent check.
- Comparator: `Integer.compare`, never `x[0] - y[0]`, which can overflow for large values.

## Variations

- **Meeting Rooms II:** the minimum number of rooms (peak overlap).
- **Meeting Rooms III:** fixed rooms with delays.
- **Merge Intervals:** combine overlaps instead of detecting them.
- **Insert a new meeting into a sorted, non-overlapping calendar:** binary search for its position and check only its two neighbours, O(log n) with a `TreeMap<start, end>` (`floorEntry` and `ceilingEntry`).

Practise it in the app: Run / Submit on this page.
