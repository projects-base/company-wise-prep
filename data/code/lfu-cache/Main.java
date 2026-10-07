import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        LFUCache cache = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "LFUCache" -> {
                    cache = new LFUCache(num(a.get(0)));
                    out.add(null);
                }
                case "get" -> out.add(cache.get(num(a.get(0))));
                case "put" -> {
                    cache.put(num(a.get(0)), num(a.get(1)));
                    out.add(null);
                }
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    private static int num(Object o) {
        return ((Number) o).intValue();
    }
}
