**Short answer:** First, merge each person's overlapping or touching busy blocks, so a person is never counted as busy twice on the same day. Then line-sweep: add `+1` at each block's `start` and `−1` at `end + 1`, and walk the change points in order. Between two change points the number of busy people is constant. Whenever `people − busy ≥ minFree`, that whole span is good. Join touching spans into maximal ranges. This is O(B log B), independent of `totalDays`.

## Picture it

Example 1: `people = 3`, `totalDays = 10`, `busy = [[0,1,3],[1,2,4],[2,6,7],[0,9,9]]`, `minFree = 3`. No person has overlapping blocks, so the merge changes nothing. The change points are:

```text
day:    1   2   4   5   6   8   9   10  11
delta: +1  +1  -1  -1  +1  -1  +1  -1   0   (11 = totalDays + 1 sentinel)
```

| Change point | Span closed | Busy in span | Free (3 − busy) | Good? | Busy after delta |
|---|---|---|---|---|---|
| 1 | — | — | — | — | 1 |
| 2 | [1, 1] | 1 | 2 | no | 2 |
| 4 | [2, 3] | 2 | 1 | no | 1 |
| 5 | [4, 4] | 1 | 2 | no | 0 |
| 6 | [5, 5] | 0 | 3 | yes → add [5,5] | 1 |
| 8 | [6, 7] | 1 | 2 | no | 0 |
| 9 | [8, 8] | 0 | 3 | yes → add [8,8] | 1 |
| 10 | [9, 9] | 1 | 2 | no | 0 |
| 11 | [10, 10] | 0 | 3 | yes → add [10,10] | 0 |

Result: `[[5,5],[8,8],[10,10]]`. Nine rows for ten days here, but with `totalDays = 10⁹` it would still be nine rows.

**The picture in one sentence:** the busy count only changes at block edges, so sweep the sorted edges and judge each whole span between them at once.

## Approach

- **Brute force:** keep a counter per day and mark every busy day for every person. That is O(totalDays + total block length). With `totalDays` up to 10⁹, it is far too slow.
- **Key insight 1, coordinate compression:** the busy count only changes at block boundaries. With B blocks there are at most 2B + 2 interesting points, so you can sweep the boundaries instead of the days.
- **Key insight 2, merge per person first:** if person 0 is busy on days 1–4 and 3–5, a plain difference array counts them twice on days 3–4. That breaks "at least P people free". Sort the blocks by `(person, start)` and merge the blocks of the same person that overlap or touch (`next.start ≤ end + 1`).
- **Sweep:** a `TreeMap<day, delta>` holds the changes. Add sentinels at day 1 and at `totalDays + 1`, so the spans before the first block and after the last one are covered. Keep a running sum. Each span `[prev, day − 1]` has the busy count of the sum before applying `day`'s delta.

## Solution

```java
import java.util.*;

class Solution {
    public int[][] freeRanges(int people, int totalDays, int[][] busy, int minFree) {
        // 1. Merge each person's blocks so overlapping blocks are not double-counted.
        int[][] blocks = busy.clone();
        Arrays.sort(blocks, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        TreeMap<Long, Integer> delta = new TreeMap<>(); // day -> change in busy-people count
        int i = 0;
        while (i < blocks.length) {
            int person = blocks[i][0];
            long s = blocks[i][1], e = blocks[i][2];
            i++;
            while (i < blocks.length && blocks[i][0] == person && blocks[i][1] <= e + 1) {
                e = Math.max(e, blocks[i][2]);
                i++;
            }
            delta.merge(s, 1, Integer::sum);
            delta.merge(e + 1, -1, Integer::sum);
        }
        delta.putIfAbsent(1L, 0);
        delta.putIfAbsent((long) totalDays + 1, 0);

        // 2. Sweep: between consecutive change points the busy count is constant.
        List<int[]> out = new ArrayList<>();
        int busyNow = 0;
        Long prev = null;
        for (Map.Entry<Long, Integer> en : delta.entrySet()) {
            long day = en.getKey();
            if (prev != null && prev <= totalDays && people - busyNow >= minFree) {
                int from = (int) (long) prev, to = (int) Math.min(day - 1, totalDays);
                if (!out.isEmpty() && out.get(out.size() - 1)[1] == from - 1) out.get(out.size() - 1)[1] = to;
                else out.add(new int[] {from, to});
            }
            busyNow += en.getValue();
            prev = day;
        }
        return out.toArray(new int[0][]);
    }
}
```

The keys are `long` because `end + 1` can be `totalDays + 1`. That is at most 10⁹ + 1, which still fits in an `int`, but `long` keeps it safe. Spans next to each other can both be good, for example when the busy count goes from 0 to 1 and `minFree` allows both. They are joined into one range, so the output has only maximal ranges.

## Complexity

- **Time:** O(B log B) for the sort and the TreeMap. The sweep is O(B).
- **Space:** O(B).

## Edge cases

- No busy blocks: one range, `[1, totalDays]`.
- A person's blocks overlap or touch (Example 3): merged, so counted once.
- Blocks ending on `totalDays`: the `−1` lands on the `totalDays + 1` sentinel, and the span from it is skipped by `prev <= totalDays`.
- `minFree = people` gives the classic "everyone is free" question.
- No good days at all: an empty list.

## Follow-ups

- **Days when at least P people are available:** that is `minFree = P` in the solution above. The per-person merge is what makes the count correct.
- **Periods of at least X consecutive days with P people available:** run the same sweep to get the maximal good ranges, then keep only those with `last − first + 1 ≥ X`. Because the ranges are maximal, no valid period is split across two of them. If they want every start day of an X-day window, each kept range `[a, b]` gives the start days `a` to `b − X + 1`.

Related: [C1 · From constraints to the expected complexity](../academy/lessons/C1.md), [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
