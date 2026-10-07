import java.util.*;

class Solution {
    public boolean canMeasureWater(int[] jugs, int target) {
        // Every reachable total is a multiple of g = gcd(capacities) (each step moves whole jugs'
        // worth or conserves water), and by Bezout every multiple of g up to the total capacity
        // is reachable.
        long total = 0;
        int g = 0;
        for (int c : jugs) {
            total += c;
            g = gcd(g, c);
        }
        return target <= total && target % g == 0;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}
