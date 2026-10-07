import java.util.*;

class Solution {
    public List<String> topKUsers(String[][] messages, int k) {
        Map<String, Integer> count = new HashMap<>();
        for (String[] m : messages) count.merge(m[0], 1, Integer::sum);
        // "better" = more messages, then smaller name. Keep the k best in a min-heap of the worst.
        Comparator<String> better = (a, b) -> {
            int ca = count.get(a), cb = count.get(b);
            return ca != cb ? Integer.compare(cb, ca) : a.compareTo(b);
        };
        PriorityQueue<String> heap = new PriorityQueue<>(better.reversed());
        for (String u : count.keySet()) {
            heap.add(u);
            if (heap.size() > k) heap.poll();
        }
        List<String> out = new ArrayList<>(heap);
        out.sort(better);
        return out;
    }
}
