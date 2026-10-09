**Short answer:** Binary search on the answer D, a real number. For a given D, a gap of length g needs `ceil(g / D) − 1` new stations so that no piece is longer than D. If the total over all gaps is ≤ k, D is achievable; smaller D needs more stations, so the test is monotonic. Search D between 0 and the largest gap for a fixed number of iterations (or until the range is below 10⁻⁶). O(n · log(range / ε)).

## Approach

- **Brute force / greedy one at a time:** repeatedly put a station into the gap whose current piece length is largest. With a scan that is O(k · n) = 2·10⁹: too slow. With a max-heap keyed by `gap / pieces` it is O(k log n), about 2·10⁷, which passes but is not the intended idea.
- **Key insight:** fix the answer and check feasibility. "Can every piece be ≤ D using ≤ k stations?" has a closed-form count per gap, and it is monotonic in D. Binary search on a real value.
- **Precision:** 100 halvings of a range up to 10⁸ get far below 10⁻⁶. Prefer a fixed iteration count over a `while (hi − lo > eps)` loop, which can spin forever when `eps` is below the floating-point spacing.
- **Exact value recovery (as in the reference):** the true optimum is `gap / pieces` for some gap. After the search, compute for each gap the fewest pieces allowed by `hi` and take the max of `gap / pieces`; that gives a clean value instead of a value that only approximates it.

## Solution

```java
class Solution {
    public double minmaxGasDist(int[] stations, int k) {
        int n = stations.length;
        double lo = 0, hi = 0;
        for (int i = 1; i < n; i++) hi = Math.max(hi, stations[i] - stations[i - 1]);
        if (hi == 0) return 0;
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            if (mid <= 0) break;
            long need = 0;
            for (int i = 1; i < n; i++) {
                need += (long) Math.ceil((stations[i] - stations[i - 1]) / mid) - 1;
            }
            if (need <= k) hi = mid; else lo = mid;
        }
        // Snap to the exact optimum: largest gap / pieces, with the fewest pieces hi allows.
        double best = 0;
        for (int i = 1; i < n; i++) {
            int gap = stations[i] - stations[i - 1];
            long pieces = Math.max(1, (long) Math.ceil(gap / hi - 1e-9));
            best = Math.max(best, (double) gap / pieces);
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n · I) with I = 100 iterations: 2·10⁵ operations.
- **Space:** O(1).

## Edge cases

- `k = 0`: the answer is the largest existing gap.
- Two stations: the answer is `gap / (k + 1)`.
- Very small D early in the search: `need` can be huge (10⁸ / 10⁻⁶), so sum into `long`.
- Floating-point equality: never compare doubles with `==`; the fixed-iteration loop avoids it.

## Follow-ups from the same round (non-DSA)

- **Multithreading basics:** a thread is an independent path of execution sharing the process's heap. Shared mutable state needs synchronisation (`synchronized`, locks, atomics) for both mutual exclusion and visibility. Prefer executors over raw threads; in Java 21, virtual threads make blocking I/O cheap. See [B1 · Threads](../academy/lessons/B1.md).
- **Replica vs snapshot vs checkpoint:** a *replica* is a continuously updated copy of the database on another node (for read scaling and failover). A *snapshot* is a point-in-time, read-only image of the data (for backups or consistent reads; MVCC gives each transaction a logical snapshot). A *checkpoint* is the moment a database flushes dirty pages from memory to disk so that crash recovery only needs to replay the write-ahead log from that point on. See [Q8 · Scaling databases](../academy/lessons/Q8.md) and [Q6 · Transactions and MVCC](../academy/lessons/Q6.md).
- **Deadlock:** needs all four Coffman conditions: mutual exclusion, hold and wait, no preemption, circular wait. *Prevention* breaks one of them, most often circular wait by always taking locks in a global order, or hold-and-wait with `tryLock` and a timeout. *Detection* builds a wait-for graph and looks for a cycle (databases such as PostgreSQL do this and abort one transaction; in the JVM, `ThreadMXBean.findDeadlockedThreads()` or a thread dump shows it). See [B2 · Locks and deadlock](../academy/lessons/B2.md).

Practise it in the app: Run / Submit on this page.
