import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<List<String>> itemTags = in.nextStringListList();
        List<List<Integer>> groups = new Solution().groupItems(itemTags);
        // Any order is accepted: sort inside each group, then sort groups by their first index.
        List<List<Integer>> canon = new ArrayList<>();
        for (List<Integer> g : groups) {
            List<Integer> c = new ArrayList<>(g);
            Collections.sort(c);
            canon.add(c);
        }
        canon.sort((a, b) -> {
            for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
                int cmp = Integer.compare(a.get(i), b.get(i));
                if (cmp != 0) return cmp;
            }
            return Integer.compare(a.size(), b.size());
        });
        IO.print(canon);
    }
}
