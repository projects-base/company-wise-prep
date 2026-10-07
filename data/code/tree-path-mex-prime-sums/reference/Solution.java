import java.util.*;

class Solution {
    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
    private int[] parent, size;

    public long[] pathMexSums(int[] values, int[][] edges) {
        int n = values.length;
        // MEX-P of a path is the smallest prime not dividing the gcd of its values, so it is
        // p(j+1) when the path is divisible by P(j) = p1*...*pj but not by p(j+1).
        //   MEX-P(path) = 2 + sum over j >= 1 of (p(j+1) - p(j)) * [every value on the path divisible by P(j)]
        // For a fixed j, the nodes divisible by P(j) form a forest; u's partners v are exactly its component.
        long[] ans = new long[n];
        Arrays.fill(ans, 2L * n);
        long prod = 1;
        for (int j = 0; j + 1 < PRIMES.length; j++) {
            prod *= PRIMES[j];
            if (prod > 1_000_000_000L) break;
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
            for (int[] e : edges) {
                if (values[e[0]] % prod == 0 && values[e[1]] % prod == 0) union(e[0], e[1]);
            }
            int gain = PRIMES[j + 1] - PRIMES[j];
            for (int u = 0; u < n; u++) {
                if (values[u] % prod == 0) ans[u] += (long) gain * size[find(u)];
            }
        }
        return ans;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private void union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a == b) return;
        if (size[a] < size[b]) { int t = a; a = b; b = t; }
        parent[b] = a;
        size[a] += size[b];
    }
}
