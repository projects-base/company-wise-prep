import java.util.*;

class Solution {
    public int minCost(int maxTime, int[][] edges, int[] passingFees) {
        int n = passingFees.length;
        final int INF = Integer.MAX_VALUE / 2;
        // best[t][v] = cheapest fees to stand in city v at exactly minute t.
        int[][] best = new int[maxTime + 1][n];
        for (int[] row : best) Arrays.fill(row, INF);
        best[0][0] = passingFees[0];
        for (int t = 0; t <= maxTime; t++) {
            for (int[] e : edges) {
                int x = e[0], y = e[1], nt = t + e[2];
                if (nt > maxTime) continue;
                if (best[t][x] < INF && best[t][x] + passingFees[y] < best[nt][y]) best[nt][y] = best[t][x] + passingFees[y];
                if (best[t][y] < INF && best[t][y] + passingFees[x] < best[nt][x]) best[nt][x] = best[t][y] + passingFees[x];
            }
        }
        int ans = INF;
        for (int t = 0; t <= maxTime; t++) ans = Math.min(ans, best[t][n - 1]);
        return ans >= INF ? -1 : ans;
    }
}
