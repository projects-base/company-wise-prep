import java.util.*;

class Solution {
    public double maxProfit(int[][] houses, int costPerHeight, int payPerHouse) {
        int n = houses.length;
        // The best height is 0 or exactly the distance of some house; compare squared distances
        // as longs so that houses at the same distance are grouped exactly.
        long[] d2 = new long[n];
        for (int i = 0; i < n; i++) d2[i] = (long) houses[i][0] * houses[i][0] + (long) houses[i][1] * houses[i][1];
        Arrays.sort(d2);
        double best = 0;
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n && d2[j] == d2[i]) j++;
            double profit = (double) payPerHouse * j - (double) costPerHeight * Math.sqrt((double) d2[i]);
            best = Math.max(best, profit);
            i = j;
        }
        return best;
    }
}
