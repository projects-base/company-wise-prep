import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] nums = in.nextIntArray();
        List<List<Integer>> ans = new Solution().threeSum(nums);
        // Any order is accepted: sort inside each triplet, then sort the triplets.
        List<List<Integer>> canon = new ArrayList<>();
        for (List<Integer> t : ans) {
            List<Integer> c = new ArrayList<>(t);
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
