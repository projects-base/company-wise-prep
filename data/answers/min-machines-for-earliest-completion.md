**Short answer:** Binary search on the finish time T. By time T, machine j can complete `⌊(T − available[j]) / duration⌋` jobs (0 if it is not yet available), and that total only grows with T, so find the smallest T whose total reaches `tasks`. For the minimum machines: the machines that became available earliest can do the most by T, so sort by availability and take them in order until their combined capacity reaches `tasks`.

## Picture it

Example 1: sorted `a = [0,2,5]`, `tasks = 5`, `duration = 3`. Search T in `[0 + 3, 0 + 5·3] = [3, 15]`. Machine j finishes ⌊(T − a[j]) / 3⌋ jobs by T.

| Step | lo | hi | mid | Jobs per machine (a = 0, 2, 5) | capacity | ≥ 5? | Action |
|---|---|---|---|---|---|---|---|
| 1 | 3 | 15 | 9 | 3, 2 (early exit) | 5 | yes | hi = 9 |
| 2 | 3 | 9 | 6 | 2, 1, 0 | 3 | no | lo = 7 |
| 3 | 7 | 9 | 8 | 2, 2, 1 | 5 | yes | hi = 8 |
| 4 | 7 | 8 | 7 | 2, 1, 0 | 3 | no | lo = 8 |

T = 8. Fewest machines: take the earliest first, 2 → 4 → 5 jobs, so M = 3. Answer `[8, 3]`.

**The picture in one sentence:** for a fixed deadline the job count is a simple sum that only grows with T, so binary search T, then the earliest machines (largest capacity) give the fewest needed.

## Approach

- **Brute force:** hand out jobs one by one, each to the machine that would finish it earliest (a min-heap of "next free time"). O(tasks · log m) — up to 10⁹ steps. Too slow.
- **Key insight:** fix a deadline T and the question becomes easy: capacity(T) is a simple sum. capacity is monotonic in T, so binary search T.
- **Bounds:** lower bound `a[0] + duration` (at least one job must finish). Upper bound `a[0] + tasks · duration` (the earliest machine alone does everything). Up to about 10¹³, so `long`, and about 44 iterations.
- **Minimum machines:** capacity per machine at time T is `⌊(T − a[j]) / d⌋`, which is larger for smaller `a[j]`. To cover `tasks` with as few machines as possible, pick the largest capacities first, which are the earliest-available machines. A sorted prefix scan gives the count.

## Solution

```java
import java.util.Arrays;

class Solution {
    public long[] earliestCompletion(int[] available, int tasks, int duration) {
        int[] a = available.clone();
        Arrays.sort(a);
        long lo = (long) a[0] + duration, hi = (long) a[0] + (long) tasks * duration;
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (capacity(a, mid, duration, tasks) >= tasks) hi = mid; else lo = mid + 1;
        }
        long t = lo;
        // Earliest-available machines do the most by T: take them in order.
        long done = 0;
        int used = 0;
        while (done < tasks) {
            done += (t - a[used]) / duration;
            used++;
        }
        return new long[] {t, used};
    }

    private static long capacity(int[] a, long t, int duration, long need) {
        long total = 0;
        for (int x : a) {
            if (x >= t) break;                 // sorted: later machines cannot help
            total += (t - x) / duration;
            if (total >= need) return total;   // early exit, also avoids overflow
        }
        return total;
    }
}
```

## Complexity

- **Time:** O(m log m) for the sort plus O(m · log(tasks · duration)) for the search: about 10⁵ × 44.
- **Space:** O(m) for the sorted copy.

## Edge cases

- One machine: `T = available + tasks · duration`, M = 1.
- Machines that become available at or after T contribute nothing and are never counted in M.
- Many machines, few tasks: T is `a[0] + duration` if enough machines are free at `a[0]`; M is the number needed.
- Overflow: `tasks · duration` up to 10¹³, so cast before multiplying. The early return in `capacity` keeps the sum small.
- The final scan always stops within the array, because capacity(T) ≥ tasks.

## Variations

- **Different job lengths:** the problem becomes scheduling to minimise makespan, which is NP-hard in general; use the LPT heuristic (longest job first to the least-loaded machine) and say so.
- **Machines with different speeds:** capacity(T) = `Σ ⌊(T − a[j]) / d[j]⌋`; the same binary search works. For minimum machines, sort by capacity at T, not by availability.
- **Queries for many task counts:** reuse the sorted array; each query is one binary search.

Practise it in the app: Run / Submit on this page.
