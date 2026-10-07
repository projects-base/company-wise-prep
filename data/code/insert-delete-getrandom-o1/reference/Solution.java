import java.util.*;

class RandomizedSet {
    // Values live in a dense list (for uniform sampling); a map gives each value's index.
    // Removal swaps the value with the last element and pops the end.
    private final List<Integer> values = new ArrayList<>();
    private final Map<Integer, Integer> index = new HashMap<>();
    private final Random random = new Random();

    public RandomizedSet() {
    }

    public boolean insert(int val) {
        if (index.containsKey(val)) return false;
        index.put(val, values.size());
        values.add(val);
        return true;
    }

    public boolean remove(int val) {
        Integer i = index.remove(val);
        if (i == null) return false;
        int last = values.remove(values.size() - 1);
        if (last != val) {
            values.set(i, last);
            index.put(last, i);
        }
        return true;
    }

    public int getRandom() {
        return values.get(random.nextInt(values.size()));
    }
}
