**Short answer:** Sweep line. Turn each shift into two events, `+1` for its worker at the start and `−1` at the end, and process the events in time order. Group them by timestamp. Keep a per-worker count of active shifts, so overlapping shifts by the same person work. Whenever the set of workers on duty actually changes at a timestamp, close the current segment (if anyone was on duty) and start a new one. That is O(n log n) for sorting plus the cost of copying the sets into the output.

## Picture it

Example 2: Ann `[0,5)`, Ann `[5,9)`, Bo `[3,7)`.

```text
time   0    3    5    7    9
Ann    |---------|---------|
Bo          |---------|
```

| t | Events at t | active after | Changed? | Emitted | onDuty after | segStart |
|---|---|---|---|---|---|---|
| 0 | Ann +1 | Ann 1 | yes (Ann on) | nothing, set was empty | {Ann} | 0 |
| 3 | Bo +1 | Ann 1, Bo 1 | yes (Bo on) | [0,3,{Ann}] | {Ann, Bo} | 3 |
| 5 | Ann −1, Ann +1 | Ann 1, Bo 1 | no (Ann stays on) | nothing, segment extends | {Ann, Bo} | 3 |
| 7 | Bo −1 | Ann 1, Bo 0 | yes (Bo off) | [3,7,{Ann, Bo}] | {Ann} | 7 |
| 9 | Ann −1 | Ann 0 | yes (Ann off) | [7,9,{Ann}] | {} | 9 |

**The picture in one sentence:** sweep the boundaries in time order with a per-worker count, and close a segment only at a timestamp where someone actually joins or leaves the on-duty set.

## Approach

- **Brute force.** Collect all start and end times, sort them, and for each elementary interval scan every shift to see who covers it. O(n²): fine for 2000 shifts, but it is not the clean answer.
- **Key insight.** The set of workers only changes at shift boundaries, so only those 2n moments matter. Process them in order and update the state incrementally.
- **Three details that make it correct:**
  1. **Group events by timestamp.** At time 5, one shift may end and another begin. Apply all of them before deciding anything, otherwise you would emit a zero-length segment.
  2. **Count shifts per worker, not a boolean.** Ann with `[0,5)` and `[3,9)` is still on duty at 5. She leaves the set only when her count drops to 0.
  3. **Emit only on a real change.** Compare each touched worker's on-duty status before and after the timestamp. If nothing changed (like Ann's touching shifts at 5), keep the segment open. That merge rule is in the spec.
- **Gaps.** When the set becomes empty, the closed segment is emitted, and no segment is emitted for the empty stretch.

## Solution

```java
import java.util.*;

class Solution {
    public List<Segment> schedule(String[] names, int[][] times) {
        // Events: +1 at a shift's start, -1 at its end, grouped by time.
        TreeMap<Integer, List<int[]>> events = new TreeMap<>(); // time -> {shift, delta}
        for (int i = 0; i < names.length; i++) {
            events.computeIfAbsent(times[i][0], k -> new ArrayList<>()).add(new int[] {i, 1});
            events.computeIfAbsent(times[i][1], k -> new ArrayList<>()).add(new int[] {i, -1});
        }
        Map<String, Integer> active = new HashMap<>(); // worker -> shifts covering now
        TreeSet<String> onDuty = new TreeSet<>();
        List<Segment> out = new ArrayList<>();
        int segStart = -1;
        for (Map.Entry<Integer, List<int[]>> e : events.entrySet()) {
            int t = e.getKey();
            Set<String> touched = new HashSet<>();
            Map<String, Boolean> before = new HashMap<>();
            for (int[] ev : e.getValue()) {
                String w = names[ev[0]];
                if (touched.add(w)) before.put(w, active.getOrDefault(w, 0) > 0);
                active.merge(w, ev[1], Integer::sum);
            }
            boolean changed = false;
            for (String w : touched) {
                if ((active.get(w) > 0) != before.get(w)) changed = true;
            }
            if (!changed) continue;                       // same set: extend the segment
            if (!onDuty.isEmpty()) out.add(new Segment(segStart, t, new ArrayList<>(onDuty)));
            for (String w : touched) {
                if (active.get(w) > 0) onDuty.add(w); else onDuty.remove(w);
            }
            segStart = t;
        }
        return out;
    }
}
```

`Segment` is provided by the judge. In your own code it would be a `record Segment(int start, int end, List<String> workers)`.

## Complexity

- **Time:** O(n log n) for the `TreeMap` of 2n events, plus O(n · W) to copy the on-duty set into up to 2n segments, where W is the number of workers on duty at once. The copying is unavoidable, because it is the size of the output.
- **Space:** O(n + output size).

## Edge cases

- One worker's shifts touch or overlap: no split at the boundary, and the worker is listed once.
- A shift ending exactly when another starts, for different workers: one segment boundary, no zero-length segment.
- Gaps with nobody on duty: left out.
- A single shift.
- Identical shifts for different workers: one segment with both names.

## Variations

- **Count only (maximum concurrent workers):** the same sweep with an integer counter (Meeting Rooms II).
- **Employee Free Time (LC 759):** the gaps this sweep skips are exactly the answer.
- **Streaming shifts or point queries ("who is on duty at time t?"):** an interval tree, or a `TreeMap` of segment starts with `floorEntry(t)`.
- **Huge time ranges:** the sweep never depends on the time values, only on the number of events, so 10⁹ is fine.

Practise it in the app: Run / Submit on this page.
