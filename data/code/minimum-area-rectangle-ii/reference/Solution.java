import java.util.*;

class Solution {
    public double minAreaFreeRect(int[][] points) {
        // Group point pairs (candidate diagonals) by doubled midpoint and squared length.
        Map<String, List<int[]>> groups = new HashMap<>();
        int n = points.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] a = points[i], b = points[j];
                long dx = a[0] - b[0], dy = a[1] - b[1];
                String key = (a[0] + b[0]) + "," + (a[1] + b[1]) + "," + (dx * dx + dy * dy);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(new int[] {i, j});
            }
        }
        long best = Long.MAX_VALUE;
        for (List<int[]> g : groups.values()) {
            for (int s = 0; s < g.size(); s++) {
                for (int t = s + 1; t < g.size(); t++) {
                    int[] p1 = points[g.get(s)[0]], p3 = points[g.get(t)[0]], p4 = points[g.get(t)[1]];
                    // corners in order p1, p3, p2, p4: area = |(p3 - p1) x (p4 - p1)|
                    long ux = p3[0] - p1[0], uy = p3[1] - p1[1];
                    long vx = p4[0] - p1[0], vy = p4[1] - p1[1];
                    long area = Math.abs(ux * vy - uy * vx);
                    best = Math.min(best, area);
                }
            }
        }
        return best == Long.MAX_VALUE ? 0.0 : (double) best;
    }
}
