import java.util.*;

class Solution {
    public long[] earliestCompletion(int[] available, int tasks, int duration) {
        int[] a = available.clone();
        Arrays.sort(a);
        // Any T where capacity(T) >= tasks works; the earliest machine alone finishes by a[0] + tasks * duration.
        long lo = (long) a[0] + duration, hi = (long) a[0] + (long) tasks * duration;
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (capacity(a, mid, duration, tasks) >= tasks) hi = mid; else lo = mid + 1;
        }
        long t = lo;
        // The earliest-available machines can do the most by T, so take them in order.
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
            if (x >= t) break; // sorted: later machines cannot help either
            total += (t - x) / duration;
            if (total >= need) return total;
        }
        return total;
    }
}
