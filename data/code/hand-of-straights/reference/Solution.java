import java.util.*;

class Solution {
    // Repeatedly take the smallest remaining card; it must begin a run of groupSize consecutive cards.
    public boolean isNStraightHand(int[] hand, int groupSize) {
        if (hand.length % groupSize != 0) return false;
        TreeMap<Integer, Integer> count = new TreeMap<>();
        for (int c : hand) count.merge(c, 1, Integer::sum);
        while (!count.isEmpty()) {
            int start = count.firstKey();
            int need = count.get(start); // that many groups must start here
            for (long v = start; v < (long) start + groupSize; v++) {
                Integer have = count.get((int) v);
                if (v > Integer.MAX_VALUE || have == null || have < need) return false;
                if (have == need) count.remove((int) v);
                else count.put((int) v, have - need);
            }
        }
        return true;
    }
}
