import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        LRUCache cache = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "LRUCache" -> { cache = new LRUCache(arg(a, 0)); out.add(null); }
                case "get" -> out.add(cache.get(arg(a, 0)));
                case "put" -> { cache.put(arg(a, 0), arg(a, 1)); out.add(null); }
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    static int arg(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }
}
