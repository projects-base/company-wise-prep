import java.util.*;

class Solution {
    public long maxGcdSum(int[] parent, int[] values) {
        int n = parent.length;
        // children lists in CSR form
        int[] childCount = new int[n + 1];
        for (int i = 1; i < n; i++) childCount[parent[i] + 1]++;
        for (int i = 0; i < n; i++) childCount[i + 1] += childCount[i];
        int[] children = new int[Math.max(0, n - 1)];
        int[] fill = Arrays.copyOf(childCount, n + 1);
        for (int i = 0; i < n; i++) if (parent[i] >= 0) children[fill[parent[i]]++] = i;

        // BFS order from the root: parents always come before children
        int[] order = new int[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        while (head < tail) {
            int u = order[head++];
            for (int e = childCount[u]; e < childCount[u + 1]; e++) order[tail++] = children[e];
        }

        long[] g = new long[n];          // GCD of the subtree of u
        long[] childGcdSum = new long[n]; // sum of g over u's children
        for (int i = n - 1; i >= 0; i--) {
            int u = order[i];
            long x = values[u];
            for (int e = childCount[u]; e < childCount[u + 1]; e++) {
                int c = children[e];
                x = gcd(x, g[c]);
                childGcdSum[u] += g[c];
            }
            g[u] = x;
        }

        // score(v) = sum over path nodes of childGcdSum - sum over non-root path nodes of g
        long[] score = new long[n];
        long best = 0;
        for (int i = 0; i < n; i++) {
            int u = order[i];
            score[u] = (u == 0 ? 0 : score[parent[u]] - g[u]) + childGcdSum[u];
            best = Math.max(best, score[u]);
        }
        return best;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}
