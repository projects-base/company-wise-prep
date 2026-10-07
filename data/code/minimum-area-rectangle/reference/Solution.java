import java.util.*;

class Solution {
    public int minAreaRect(int[][] points) {
        Set<Long> set = new HashSet<>();
        for (int[] p : points) set.add(key(p[0], p[1]));
        long best = Long.MAX_VALUE;
        int n = points.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] a = points[i], b = points[j];
                if (a[0] == b[0] || a[1] == b[1]) continue; // need a proper diagonal
                if (set.contains(key(a[0], b[1])) && set.contains(key(b[0], a[1]))) {
                    long area = (long) Math.abs(a[0] - b[0]) * Math.abs(a[1] - b[1]);
                    best = Math.min(best, area);
                }
            }
        }
        return best == Long.MAX_VALUE ? 0 : (int) best;
    }

    private static long key(int x, int y) {
        return (long) x * 100_000L + y;
    }
}
