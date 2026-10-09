**Short answer:** Sort task indices by enqueue time and keep a min-heap of available tasks ordered by (processing time, index). Simulate a `long` clock: if nothing is ready, jump the clock to the next enqueue time; push every task that has arrived by now; pop the best one, record it, and advance the clock by its processing time. That is O(n log n).

## Approach

- **Brute force:** at each step, scan all unfinished tasks for the shortest available one. O(n²), too slow for 10⁵ tasks.
- **Key insight:** this is an event simulation with two orderings. Arrival order decides *when* a task becomes eligible (sort once). Processing time decides *which* eligible task runs (a heap). Each task enters and leaves the heap once.
- **Details that trip people up:**
  - Sort **indices**, not the tasks themselves, so the original index survives for the output and the tie-break.
  - When the heap is empty and the CPU is idle, jump the clock forward instead of ticking one unit at a time (times go up to 10⁹).
  - The clock is a `long`: up to 10⁵ tasks × 10⁹ processing time overflows `int`.
  - A task arriving exactly when the previous one finishes is available (`<=`).

## Solution

```java
import java.util.*;

class Solution {
    public int[] getOrder(int[][] tasks) {
        int n = tasks.length;
        Integer[] byTime = new Integer[n];
        for (int i = 0; i < n; i++) byTime[i] = i;
        Arrays.sort(byTime, (a, b) -> Integer.compare(tasks[a][0], tasks[b][0]));
        PriorityQueue<Integer> ready = new PriorityQueue<>((a, b) ->
                tasks[a][1] != tasks[b][1]
                        ? Integer.compare(tasks[a][1], tasks[b][1])
                        : Integer.compare(a, b));
        int[] order = new int[n];
        long time = 0;
        int next = 0, done = 0;
        while (done < n) {
            // Idle with nothing ready: jump to the next arrival.
            if (ready.isEmpty() && time < tasks[byTime[next]][0]) time = tasks[byTime[next]][0];
            while (next < n && tasks[byTime[next]][0] <= time) ready.add(byTime[next++]);
            int t = ready.poll();
            order[done++] = t;
            time += tasks[t][1];
        }
        return order;
    }
}
```

## Complexity

- **Time:** O(n log n): the sort, plus one heap push and pop per task.
- **Space:** O(n) for the index array and the heap.

## Edge cases

- All tasks arrive together: pure shortest-job-first with index tie-break (example 2).
- Long idle gaps between arrivals: the clock jump handles them.
- Equal processing times: smallest index wins, via the comparator.
- Huge totals: `long` clock.

## Variations

- **Multiple CPUs:** add a second heap of busy CPUs keyed by finish time (this is the "process tasks using servers" pattern).
- **Preemptive shortest-remaining-time-first:** at each arrival, compare the new task with the running one's remaining time and swap if shorter.

See [C3 · Hidden time costs](../academy/lessons/C3.md) for the boxing cost of `Integer[]` and comparator lambdas.

Practise it in the app: Run / Submit on this page.
