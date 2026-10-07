import java.util.*;

class Solution {
    private int[] parent;

    public List<List<Integer>> groupItems(List<List<String>> itemTags) {
        int n = itemTags.size();
        parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        Map<String, Integer> owner = new HashMap<>(); // tag -> first item seen with it
        for (int i = 0; i < n; i++) {
            for (String tag : itemTags.get(i)) {
                Integer o = owner.putIfAbsent(tag, i);
                if (o != null) union(o, i);
            }
        }
        Map<Integer, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) groups.computeIfAbsent(find(i), k -> new ArrayList<>()).add(i);
        return new ArrayList<>(groups.values());
    }

    private int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private void union(int a, int b) {
        parent[find(a)] = find(b);
    }
}
