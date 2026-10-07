import java.util.*;

class RangeModule {
    // disjoint, non-touching intervals: start -> end (half-open)
    private final TreeMap<Integer, Integer> map = new TreeMap<>();

    public RangeModule() {
    }

    public void addRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.floorEntry(left);
        if (e != null && e.getValue() >= left) {
            left = e.getKey();
            right = Math.max(right, e.getValue());
        }
        // absorb every interval starting inside [left, right]
        Map.Entry<Integer, Integer> nx = map.ceilingEntry(left);
        while (nx != null && nx.getKey() <= right) {
            right = Math.max(right, nx.getValue());
            map.remove(nx.getKey());
            nx = map.ceilingEntry(left);
        }
        map.put(left, right);
    }

    public boolean queryRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.floorEntry(left);
        return e != null && e.getValue() >= right;
    }

    public void removeRange(int left, int right) {
        Map.Entry<Integer, Integer> e = map.lowerEntry(left);
        if (e != null && e.getValue() > left) {
            int end = e.getValue();
            map.put(e.getKey(), left);
            if (end > right) map.put(right, end);
        }
        Map.Entry<Integer, Integer> nx = map.ceilingEntry(left);
        while (nx != null && nx.getKey() < right) {
            map.remove(nx.getKey());
            if (nx.getValue() > right) map.put(right, nx.getValue());
            nx = map.ceilingEntry(left);
        }
    }
}
