**Short answer:** Sort the bookings by start. Keep a current merged interval. For each next booking, if it starts at or before the current end, extend the end to the larger of the two ends; otherwise close the current interval and start a new one. Here touching bookings (`[1,4]`, `[4,5]`) also merge, so the test is `start ≤ end`. O(n log n).

## Picture it

`intervals = [[8,10],[1,3],[2,6],[3,4],[15,18],[10,11]]`, sorted by start: `[1,3] [2,6] [3,4] [8,10] [10,11] [15,18]`. Start with the block `[1,3]`.

| i | Interval | Current block before | Test | Action | Block after | out |
|---|---|---|---|---|---|---|
| 1 | [2,6] | [1,3] | 2 ≤ 3 | extend, e = max(3,6) | [1,6] | — |
| 2 | [3,4] | [1,6] | 3 ≤ 6 | inside, e stays max(6,4) = 6 | [1,6] | — |
| 3 | [8,10] | [1,6] | 8 > 6 | close [1,6], start new | [8,10] | [1,6] |
| 4 | [10,11] | [8,10] | 10 ≤ 10 (touch) | extend | [8,11] | [1,6] |
| 5 | [15,18] | [8,11] | 15 > 11 | close [8,11], start new | [15,18] | [1,6] [8,11] |
| end | — | [15,18] | — | close the last block | — | [1,6] [8,11] [15,18] |

**The picture in one sentence:** after sorting by start, a block only ever grows to the right, so one pass with "start ≤ current end → extend (keep the max end), else close" merges everything.

## Approach

- **Brute force:** repeatedly find any two overlapping intervals and merge them until none overlap. O(n²) or worse.
- **Key insight:** after sorting by start, any interval that overlaps the current merged block must come right after it in order. Once a booking starts after the current end, nothing later can reach back into the current block, so it can be closed.
- **Keep the max end:** a later booking can be fully inside the current one (`[1,10]`, `[2,3]`); the end must not shrink to 3.

## Solution

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Solution {
    public int[][] merge(int[][] intervals) {
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        List<int[]> out = new ArrayList<>();
        int s = a[0][0], e = a[0][1];
        for (int i = 1; i < a.length; i++) {
            if (a[i][0] <= e) {
                e = Math.max(e, a[i][1]);        // overlaps or touches: extend
            } else {
                out.add(new int[] {s, e});       // gap: close the block
                s = a[i][0];
                e = a[i][1];
            }
        }
        out.add(new int[] {s, e});
        return out.toArray(new int[0][]);
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort, O(n) for the scan.
- **Space:** O(n) for the copy and the output.

## Edge cases

- One booking: returned as is.
- Touching bookings merge here because the times are inclusive (a car booked until 4 and from 4). If the domain used half-open times and wanted a gap kept, the test would be `<`. Confirm with the interviewer.
- Nested bookings: keep `max(e, a[i][1])`.
- Zero-length bookings (`start == end`): allowed and handled.
- Unsorted input: always sort first.

## Variations

- **Insert Interval:** the list is already sorted and non-overlapping; add one interval in O(n) without sorting.
- **Bookings arriving one at a time:** a `TreeMap<start, end>`; on insert, look at `floorEntry(start)` and absorb every following entry whose start ≤ the new end. O(log n) amortised per insert.
- **Car rental twist:** merging per car is the same algorithm grouped by car id (`Map<carId, List<int[]>>`).
- **Total booked time:** merge, then sum the lengths.
- **Minimum cars needed for all bookings:** that is Meeting Rooms II (peak overlap).

Practise it in the app: Run / Submit on this page.
