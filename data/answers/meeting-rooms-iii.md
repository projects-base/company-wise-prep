**Short answer:** Simulate with two heaps. Sort meetings by start. Keep a min-heap of free room numbers and a min-heap of busy rooms ordered by (end time, room number). For each meeting, first release every busy room that has ended by its start. If a room is free, take the lowest number. Otherwise take the busy room that frees first, and the meeting runs from that room's end time for its original duration. Count meetings per room. O(m log n), with `long` times.

## Picture it

Example 1: `n = 2`, meetings sorted by start `[0,10] [1,5] [2,7] [3,4]`. `busy` holds {end, room}, earliest end first.

| Meeting | Released (end ≤ start) | free before | Room chosen | Runs | busy after | count |
|---|---|---|---|---|---|---|
| [0,10] | none | {0,1} | 0 (lowest free) | [0,10) | {10,r0} | [1,0] |
| [1,5] | none | {1} | 1 (lowest free) | [1,5) | {5,r1} {10,r0} | [1,1] |
| [2,7] | none (5 > 2) | {} | 1 (frees first, at 5) | [5,10) delayed | {10,r0} {10,r1} | [1,2] |
| [3,4] | none (10 > 3) | {} | 0 (ties at 10, lower number) | [10,11) delayed | {10,r1} {11,r0} | [2,2] |

Counts tie at 2, so the answer is room 0.

**The picture in one sentence:** free rooms are ordered by number and busy rooms by end time, so keep one heap for each and release finished rooms before choosing.

## Approach

- **Brute force:** for each meeting, scan all n rooms to find a free one or the earliest to free. O(m · n) = 10⁷, which actually passes for n ≤ 100. Say so, then offer the heaps for the general case.
- **Key insight:** two different orders matter. Among free rooms, the lowest *number* wins; among busy rooms, the earliest *end time* wins (then the lowest number). One heap for each order.
- **Delays chain:** a delayed meeting starts when its room frees, not at its original start. Store the new end, `freeTime + duration`, so later meetings see the delay.
- **Release before choosing:** move all rooms with `end ≤ start` into the free heap first. Otherwise you might pick a "busy" room with a higher number while a lower-numbered room is actually free.

## Solution

```java
import java.util.Arrays;
import java.util.PriorityQueue;

class Solution {
    public int mostBooked(int n, int[][] meetings) {
        int[][] ms = meetings.clone();
        Arrays.sort(ms, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> free = new PriorityQueue<>();
        for (int i = 0; i < n; i++) free.add(i);
        // busy rooms: {endTime, room}, earliest end first, then lowest room
        PriorityQueue<long[]> busy = new PriorityQueue<>(
            (a, b) -> a[0] != b[0] ? Long.compare(a[0], b[0]) : Long.compare(a[1], b[1]));
        int[] count = new int[n];
        for (int[] m : ms) {
            long start = m[0], dur = m[1] - m[0];
            while (!busy.isEmpty() && busy.peek()[0] <= start) free.add((int) busy.poll()[1]);
            if (!free.isEmpty()) {
                int room = free.poll();
                count[room]++;
                busy.add(new long[] {start + dur, room});
            } else {
                long[] b = busy.poll();                  // wait for the earliest room
                int room = (int) b[1];
                count[room]++;
                busy.add(new long[] {b[0] + dur, room}); // delayed, same duration
            }
        }
        int best = 0;
        for (int i = 1; i < n; i++) if (count[i] > count[best]) best = i;  // ties keep lowest
        return best;
    }
}
```

## Complexity

- **Time:** O(m log m) to sort plus O(m log n) for the heaps. Each room moves between heaps at most once per meeting.
- **Space:** O(n + m) (the sorted copy is O(m)).

## Edge cases

- One room: every meeting goes there; answer 0.
- Ties in counts: the strict `>` keeps the lowest index.
- Ties in busy end times: the comparator breaks them by room number.
- Delays accumulate: with 10⁵ meetings of length up to 5·10⁵ queued on one room, end times reach about 5·10¹⁰, beyond `int`. Use `long`.
- The waiting queue order (rule 3) is automatic: meetings are processed in original start order, and each one either gets a room at once or the earliest free one.

## Variations

- **Return the schedule:** record (room, actualStart, actualEnd) for each meeting.
- **Meetings may be dropped instead of delayed:** if nothing is free, skip it; the busy heap still gives the earliest end.
- **Meeting Rooms II:** unlimited rooms, count the minimum needed.

Practise it in the app: Run / Submit on this page.
