import java.util.*;

class Solution {
    private int[] parent, size;

    public int[] countReachable(int n, int[][] edges, int[] queries) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        for (int[] e : edges) union(e[0], e[1]);
        int[] ans = new int[queries.length];
        for (int i = 0; i < queries.length; i++) ans[i] = size[find(queries[i])] - 1;
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
