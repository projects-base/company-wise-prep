import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] orderIds = in.nextIntArray();
        String[] statuses = in.nextStringArray();
        List<List<Integer>> ans = new Solution().classifyOrders(orderIds, statuses);
        // Each list may be in any order, so sort both before printing.
        List<List<Integer>> canon = new ArrayList<>();
        for (List<Integer> l : ans) {
            List<Integer> c = new ArrayList<>(l);
            Collections.sort(c);
            canon.add(c);
        }
        IO.print(canon);
    }
}
