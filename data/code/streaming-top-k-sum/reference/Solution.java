import java.util.*;

class TopKSum {
    private final int k;
    private final Map<Integer, Integer> value = new HashMap<>();
    // Items ordered by (value, id). `top` holds the k largest, `rest` everything else.
    private final TreeSet<Integer> top, rest;
    private long sum;

    public TopKSum(int k) {
        this.k = k;
        Comparator<Integer> cmp = (a, b) -> {
            int va = value.get(a), vb = value.get(b);
            return va != vb ? Integer.compare(va, vb) : Integer.compare(a, b);
        };
        top = new TreeSet<>(cmp);
        rest = new TreeSet<>(cmp);
    }

    public void upsert(int id, int v) {
        remove(id);
        value.put(id, v);
        top.add(id);
        sum += v;
        if (top.size() > k) {
            int low = top.pollFirst();
            sum -= value.get(low);
            rest.add(low);
        }
    }

    public void remove(int id) {
        if (!value.containsKey(id)) return;
        if (top.remove(id)) {
            sum -= value.get(id);
            if (!rest.isEmpty()) {
                int up = rest.pollLast();
                top.add(up);
                sum += value.get(up);
            }
        } else {
            rest.remove(id);
        }
        value.remove(id);
    }

    public long topKSum() {
        return sum;
    }
}
