import java.util.*;

class PartnerNetwork {
    private final Set<Long> direct = new HashSet<>();
    private final Map<Integer, Integer> parent = new HashMap<>();

    public PartnerNetwork(int[][] partnerships) {
        for (int[] p : partnerships) {
            direct.add(key(p[0], p[1]));
            union(p[0], p[1]);
        }
    }

    public boolean areConnected(int x, int y) {
        return direct.contains(key(x, y));
    }

    public boolean areRelated(int x, int y) {
        return !areConnected(x, y) && find(x) == find(y);
    }

    // Order-independent key for the pair {x, y}.
    private static long key(int x, int y) {
        int a = Math.min(x, y), b = Math.max(x, y);
        return ((long) a << 32) | (b & 0xffffffffL);
    }

    private int find(int x) {
        Integer p = parent.get(x);
        if (p == null) return x; // never seen: its own network
        int root = x;
        while (parent.get(root) != root) root = parent.get(root);
        while (x != root) { // path compression
            int nx = parent.get(x);
            parent.put(x, root);
            x = nx;
        }
        return root;
    }

    private void union(int a, int b) {
        parent.putIfAbsent(a, a);
        parent.putIfAbsent(b, b);
        int ra = find(a), rb = find(b);
        if (ra != rb) parent.put(ra, rb);
    }
}
