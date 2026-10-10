**Short answer:** The number of rooms needed is the maximum number of meetings running at the same moment. Sort the start times and the end times separately. Sweep the starts; before counting a new meeting, free every room whose meeting ended at or before this start. Track the peak count. O(n log n). The priority-queue version keeps a min-heap of end times instead.

## Picture it

Example 1: `[[0,30],[5,10],[15,20]]` → `starts = [0,5,15]`, `ends = [10,20,30]` (two-pointer version).

| i | starts[i] | j before | Freed (ends[j] ≤ start) | rooms | best |
|---|---|---|---|---|---|
| 0 | 0 | 0 | none (10 > 0) | 1 | 1 |
| 1 | 5 | 0 | none (10 > 5) | 2 | 2 |
| 2 | 15 | 0 | end 10 → j = 1, rooms 2 → 1; then 20 > 15 stops | 2 | 2 |

Answer 2. The heap version sees the same thing: heap of end times `{30}` → `{10,30}` → poll 10, add 20 → `{20,30}`, size 2.

**The picture in one sentence:** rooms needed is the peak overlap, and you only need to count how many meetings have ended before each start, so starts and ends can be sorted separately.

## Approach

- **Brute force:** for each meeting's start, count how many meetings contain that moment. O(n²).
- **Key insight:** rooms needed = peak overlap. The overlap only changes at start and end points, so a sweep over sorted events finds the peak.
- **Two pointers:** you do not need to know *which* meeting ended, only *how many* have ended by a given start. So starts and ends can be sorted independently.
- **Min-heap:** sort meetings by start; the heap holds the end times of rooms in use. If the earliest end is ≤ the new start, reuse that room (poll). Push the new end. The heap's maximum size is the answer.
- **Tie rule:** a meeting ending at t frees its room for one starting at t, so use `≤` when freeing.

## Solution

Two-pointer version:

```java
import java.util.Arrays;

class Solution {
    public int minMeetingRooms(int[][] intervals) {
        int n = intervals.length;
        int[] starts = new int[n], ends = new int[n];
        for (int i = 0; i < n; i++) { starts[i] = intervals[i][0]; ends[i] = intervals[i][1]; }
        Arrays.sort(starts);
        Arrays.sort(ends);
        int rooms = 0, best = 0, j = 0;
        for (int i = 0; i < n; i++) {
            while (ends[j] <= starts[i]) { j++; rooms--; }  // rooms freed by now
            rooms++;
            best = Math.max(best, rooms);
        }
        return best;
    }
}
```

Priority-queue version:

```java
import java.util.Arrays;
import java.util.PriorityQueue;

class Solution {
    public int minMeetingRooms(int[][] intervals) {
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();   // end times of busy rooms
        for (int[] m : a) {
            if (!ends.isEmpty() && ends.peek() <= m[0]) ends.poll();  // reuse that room
            ends.add(m[1]);
        }
        return ends.size();
    }
}
```

The heap only ever grows when no room is free, so its final size equals the peak.

## Complexity

- **Time:** O(n log n) for both (sorting; heap operations are O(log n) each).
- **Space:** O(n) for the arrays or the heap.

## Edge cases

- Back-to-back meetings (`[1,5],[5,10]`): one room, thanks to `≤`.
- Identical intervals: one room each.
- One meeting: 1.
- Unsorted input: both versions sort first.

## Follow-ups

- **Both versions:** shown above. The two-pointer one is lighter (primitive arrays, no boxing); the heap one generalises to "which room does each meeting get".
- **The unspecified variant:** the common ones are:
  - *Return the room for each meeting:* heap of (end, roomId) plus a pool of free ids.
  - *When is the peak, and which meetings overlap then?* Same sweep; remember the start time at which `best` was updated.
  - *Fixed number of rooms, meetings get delayed:* that is [Meeting Rooms III](meeting-rooms-iii.md).
  - *Large time range, many queries "rooms needed at time t":* a difference array over time, then a prefix sum.

Practise it in the app: Run / Submit on this page.
