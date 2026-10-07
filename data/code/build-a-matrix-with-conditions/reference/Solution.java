import java.util.*;

class Solution {
    public int[][] buildMatrix(int k, int[][] rowConditions, int[][] colConditions) {
        int[] rowOrder = topoOrder(k, rowConditions);
        int[] colOrder = topoOrder(k, colConditions);
        if (rowOrder == null || colOrder == null) return new int[0][];
        int[] rowOf = new int[k + 1], colOf = new int[k + 1];
        for (int i = 0; i < k; i++) {
            rowOf[rowOrder[i]] = i;
            colOf[colOrder[i]] = i;
        }
        int[][] m = new int[k][k];
        for (int v = 1; v <= k; v++) m[rowOf[v]][colOf[v]] = v;
        return m;
    }

    /** Kahn's algorithm over 1..k; null if the conditions contain a cycle. */
    @SuppressWarnings("unchecked")
    private int[] topoOrder(int k, int[][] conds) {
        List<Integer>[] out = new List[k + 1];
        for (int v = 1; v <= k; v++) out[v] = new ArrayList<>();
        int[] indeg = new int[k + 1];
        for (int[] c : conds) {
            out[c[0]].add(c[1]);
            indeg[c[1]]++;
        }
        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 1; v <= k; v++) if (indeg[v] == 0) q.add(v);
        int[] order = new int[k];
        int n = 0;
        while (!q.isEmpty()) {
            int v = q.poll();
            order[n++] = v;
            for (int w : out[v]) if (--indeg[w] == 0) q.add(w);
        }
        return n == k ? order : null;
    }
}
