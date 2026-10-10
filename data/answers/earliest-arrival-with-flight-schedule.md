**Short answer:** Keep `best[a]`, the earliest time the package can be at airport `a`, and start with `best[source] = startTime`. Sort the flights by departure time and scan them once. A flight is usable if `best[from] ≤ departure`. If it is usable and lands earlier than `best[to]`, set `best[to]` to its arrival time. Return `best[destination]`, or `-1` if it was never set. The sort makes it O(F log F).

## Picture it

Example 1, `source = 0`, `destination = 3`, `startTime = 0`. Flights sorted by departure, scanned once:

| Step | Flight (from → to, dep → arr) | `best[from] ≤ dep`? | Lands earlier than `best[to]`? | `best` [0..3] after |
|---|---|---|---|---|
| start | – | – | – | [0, ∞, ∞, ∞] |
| 1 | 0 → 1, 1 → 4 | 0 ≤ 1 yes | 4 < ∞ yes | [0, 4, ∞, ∞] |
| 2 | 0 → 2, 2 → 9 | 0 ≤ 2 yes | 9 < ∞ yes | [0, 4, 9, ∞] |
| 3 | 1 → 2, 5 → 7 | 4 ≤ 5 yes | 7 < 9 yes | [0, 4, 7, ∞] |
| 4 | 2 → 3, 8 → 10 | 7 ≤ 8 yes | 10 < ∞ yes | [0, 4, 7, 10] |
| 5 | 2 → 3, 10 → 12 | 7 ≤ 10 yes | 12 < 10 no | unchanged |

Answer **10**. Step 3 is the point: the two-flight chain through airport 1 beats the direct flight to 2, and it is in place before the 8 o'clock departure is scanned.

**The picture in one sentence:** sorted by departure, every flight that could feed a connection is scanned before it, so one pass of "can I board, and do I land earlier?" settles every airport.

## Approach

- **Brute force:** a DFS over every chain of connecting flights, tracking the current time. This is exponential in the worst case.
- **Dijkstra view:** this is an earliest-arrival shortest-path problem. Pop the airport with the smallest known time, then relax each outgoing flight whose `departure ≥ time`. It is correct because time only moves forward along a route. It costs O((N + F) log N) and needs flights grouped by airport.
- **Key insight (simpler):** sort the flights by departure. Any flight `g` that can feed flight `f` must land no later than `f` departs. Every flight departs before it lands (`departure < arrival`), so `g` departs strictly earlier than `f` and is scanned first. By the time we reach `f`, `best[f.from]` already includes every flight that could help it, so one pass is enough. This is the core of the Connection Scan Algorithm used in public-transport routing.
- The fewest flights is not the goal. A longer chain can land earlier, and the scan handles that without special cases.

## Solution

```java
import java.util.*;

class Solution {
    public int earliestArrival(int n, int[][] flights, int source, int destination, int startTime) {
        long[] best = new long[n];
        Arrays.fill(best, Long.MAX_VALUE);
        best[source] = startTime;
        int[][] byDeparture = flights.clone();
        Arrays.sort(byDeparture, (a, b) -> Integer.compare(a[2], b[2]));
        for (int[] f : byDeparture) {
            // f = [from, to, departure, arrival]
            if (best[f[0]] <= f[2] && f[3] < best[f[1]]) best[f[1]] = f[3];
        }
        return best[destination] == Long.MAX_VALUE ? -1 : (int) best[destination];
    }
}
```

Two flights with the same departure time cannot feed each other. One of them would have to land at that same moment, but it departed before it landed. So the order of ties does not matter.

## Complexity

- **Time:** O(F log F) for the sort, O(F) for the scan and O(N) to set up `best`.
- **Space:** O(N + F) for `best` and the cloned array of row references.

## Edge cases

- `source == destination`: the answer is `startTime`. It is set before the scan, and the scan only ever lowers a value.
- The package reaches an airport after the only onward flight has left: `-1` (Example 2).
- The package is ready after the flight departs: `-1` (Example 3).
- A zero-minute layover (`arrival == next departure`) is allowed because the check uses `≤`.
- No flights at all: the answer is `-1` unless `source == destination`.

## Variations

- **Minimum layover `L`:** check `best[from] + L ≤ departure`. Usually the rule does not apply at the source.
- **Return the route:** store the flight that last improved each airport, then walk back from the destination.
- **Latest departure that still arrives by time T:** run the same scan backwards, with flights sorted by arrival time, latest first.

Related: [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
