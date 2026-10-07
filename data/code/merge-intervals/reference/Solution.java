import java.util.*;

class Solution {
    public int[][] merge(int[][] intervals) {
        int[][] a = intervals.clone();
        Arrays.sort(a, (x, y) -> Integer.compare(x[0], y[0]));
        List<int[]> out = new ArrayList<>();
        int s = a[0][0], e = a[0][1];
        for (int i = 1; i < a.length; i++) {
            if (a[i][0] <= e) e = Math.max(e, a[i][1]);
            else {
                out.add(new int[] {s, e});
                s = a[i][0];
                e = a[i][1];
            }
        }
        out.add(new int[] {s, e});
        return out.toArray(new int[0][]);
    }
}
