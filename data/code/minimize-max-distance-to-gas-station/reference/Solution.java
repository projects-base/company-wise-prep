import java.util.*;

class Solution {
    public double minmaxGasDist(int[] stations, int k) {
        int n = stations.length;
        double lo = 0, hi = 0;
        for (int i = 1; i < n; i++) hi = Math.max(hi, stations[i] - stations[i - 1]);
        if (hi == 0) return 0;
        // Binary search the answer D: stations needed so every gap is <= D.
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            if (mid <= 0) break;
            long need = 0;
            for (int i = 1; i < n; i++) need += (long) Math.ceil((stations[i] - stations[i - 1]) / mid) - 1;
            if (need <= k) hi = mid;
            else lo = mid;
        }
        // hi is feasible and within rounding error of the optimum; recover the exact value
        // as the largest gap / pieces when each gap uses the fewest pieces allowed by hi.
        double best = 0;
        for (int i = 1; i < n; i++) {
            int gap = stations[i] - stations[i - 1];
            long pieces = Math.max(1, (long) Math.ceil(gap / hi - 1e-9));
            best = Math.max(best, (double) gap / pieces);
        }
        return best;
    }
}
