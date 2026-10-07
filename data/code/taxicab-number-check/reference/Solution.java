import java.util.*;

class Solution {
    public boolean isTaxicab(long n) {
        // Two pointers: a grows from 1, b shrinks from the largest value with b^3 < n.
        // With n <= 2e18 both a and b stay below 1.3e6, so a^3 + b^3 fits in a long.
        long b = (long) Math.cbrt((double) n);
        while (b > 0 && b * b * b >= n) b--;          // fix floating-point error downwards
        while ((b + 1) * (b + 1) * (b + 1) < n) b++;  // ... and upwards
        long a = 1;
        int ways = 0;
        while (a <= b) {
            long s = a * a * a + b * b * b;
            if (s == n) { ways++; a++; b--; }
            else if (s < n) a++;
            else b--;
        }
        return ways == 2;
    }
}
