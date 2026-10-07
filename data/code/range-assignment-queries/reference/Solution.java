import java.util.*;

class Solution {
    private int[] next; // next[i]: smallest index >= i not yet written (n means none)

    public int[] applyAssignments(int[] arr, int[][] queries) {
        int n = arr.length;
        int[] out = arr.clone();
        next = new int[n + 1];
        for (int i = 0; i <= n; i++) next[i] = i;
        // The last query covering a cell decides its value, so go backwards and write each cell once.
        for (int q = queries.length - 1; q >= 0; q--) {
            int l = queries[q][0], r = queries[q][1], k = queries[q][2];
            for (int i = find(l); i <= r; i = find(i)) {
                out[i] = k;
                next[i] = i + 1;
            }
        }
        return out;
    }

    private int find(int x) {
        int root = x;
        while (next[root] != root) root = next[root];
        while (next[x] != root) {
            int t = next[x];
            next[x] = root;
            x = t;
        }
        return root;
    }
}
