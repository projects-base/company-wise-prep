import java.util.*;

class LFUCache {
    private final int capacity;
    private final Map<Integer, Integer> values = new HashMap<>();
    private final Map<Integer, Integer> counts = new HashMap<>();
    // use count -> keys with that count, least recently used first
    private final Map<Integer, LinkedHashSet<Integer>> buckets = new HashMap<>();
    private int minCount = 0;

    public LFUCache(int capacity) {
        this.capacity = capacity;
    }

    public int get(int key) {
        Integer v = values.get(key);
        if (v == null) return -1;
        touch(key);
        return v;
    }

    public void put(int key, int value) {
        if (capacity <= 0) return;
        if (values.containsKey(key)) {
            values.put(key, value);
            touch(key);
            return;
        }
        if (values.size() >= capacity) {
            LinkedHashSet<Integer> bucket = buckets.get(minCount);
            int victim = bucket.iterator().next();
            bucket.remove(victim);
            if (bucket.isEmpty()) buckets.remove(minCount);
            values.remove(victim);
            counts.remove(victim);
        }
        values.put(key, value);
        counts.put(key, 1);
        buckets.computeIfAbsent(1, c -> new LinkedHashSet<>()).add(key);
        minCount = 1;
    }

    private void touch(int key) {
        int c = counts.get(key);
        LinkedHashSet<Integer> bucket = buckets.get(c);
        bucket.remove(key);
        if (bucket.isEmpty()) {
            buckets.remove(c);
            if (minCount == c) minCount = c + 1;
        }
        counts.put(key, c + 1);
        buckets.computeIfAbsent(c + 1, x -> new LinkedHashSet<>()).add(key);
    }
}
