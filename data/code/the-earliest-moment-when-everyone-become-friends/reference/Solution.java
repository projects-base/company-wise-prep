import java.util.*;

class Solution {
    private int[] parent;

    public int earliestAcq(int[][] logs, int n) {
        int[][] sorted = logs.clone();
        Arrays.sort(sorted, Comparator.comparingInt(a -> a[0]));
        parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        int groups = n;
        for (int[] l : sorted) {
            int a = find(l[1]), b = find(l[2]);
            if (a != b) {
                parent[a] = b;
                if (--groups == 1) return l[0];
            }
        }
        return -1;
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }
}
