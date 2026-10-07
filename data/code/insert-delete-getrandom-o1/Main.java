import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        RandomizedSet set = null;
        Set<Integer> model = new HashSet<>();            // what the set should contain
        Map<Integer, Integer> seen = new HashMap<>();     // getRandom: times each value came back
        Map<Integer, Double> expected = new HashMap<>();  // getRandom: expected times under uniformity
        int randomCalls = 0;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "RandomizedSet" -> { set = new RandomizedSet(); out.add(null); }
                case "insert" -> { model.add(arg(a)); out.add(set.insert(arg(a))); }
                case "remove" -> { model.remove(arg(a)); out.add(set.remove(arg(a))); }
                case "getRandom" -> {
                    int v = set.getRandom();
                    randomCalls++;
                    out.add(model.contains(v) ? (Object) true : (Object) v);
                    seen.merge(v, 1, Integer::sum);
                    double share = 1.0 / model.size();
                    for (int x : model) expected.merge(x, share, Double::sum);
                }
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        // Uniformity check, only where there is enough data for it to be meaningful (6 standard deviations).
        if (randomCalls >= 1000) {
            for (Map.Entry<Integer, Double> e : expected.entrySet()) {
                double exp = e.getValue();
                int got = seen.getOrDefault(e.getKey(), 0);
                if (exp >= 50 && Math.abs(got - exp) > 6 * Math.sqrt(exp) + 5) {
                    out.add(String.format(Locale.ROOT, "getRandom is not uniform: %d came back %d times, expected about %.0f", e.getKey(), got, exp));
                    break;
                }
            }
        }
        IO.print(out);
    }

    static int arg(List<?> a) {
        return ((Number) a.get(0)).intValue();
    }
}
