import java.util.*;

class FirstUnique {
    private final Map<Integer, Integer> count = new HashMap<>();
    private final ArrayDeque<Integer> queue = new ArrayDeque<>(); // arrival order of first sightings

    public FirstUnique(int[] nums) {
        for (int x : nums) add(x);
    }

    public int showFirstUnique() {
        // lazily drop numbers that have become duplicates
        while (!queue.isEmpty() && count.get(queue.peek()) > 1) queue.poll();
        return queue.isEmpty() ? -1 : queue.peek();
    }

    public void add(int value) {
        int c = count.merge(value, 1, Integer::sum);
        if (c == 1) queue.add(value);
    }
}
