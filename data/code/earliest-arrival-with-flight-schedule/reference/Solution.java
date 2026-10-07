import java.util.*;

class Solution {
    // Scan flights in order of departure. Any flight that could deliver the package to an airport
    // in time for flight f departs strictly before f (it departs before it lands, and it lands no
    // later than f leaves), so by the time f is considered, best[f.from] is already final.
    public int earliestArrival(int n, int[][] flights, int source, int destination, int startTime) {
        long[] best = new long[n];
        Arrays.fill(best, Long.MAX_VALUE);
        best[source] = startTime;
        int[][] byDeparture = flights.clone();
        Arrays.sort(byDeparture, (a, b) -> Integer.compare(a[2], b[2]));
        for (int[] f : byDeparture) {
            if (best[f[0]] <= f[2] && f[3] < best[f[1]]) best[f[1]] = f[3];
        }
        return best[destination] == Long.MAX_VALUE ? -1 : (int) best[destination];
    }
}
