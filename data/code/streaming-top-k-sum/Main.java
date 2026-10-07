import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        TopKSum s = null;
        for (int i = 0; i < ops.size(); i++) {
            List<?> a = (List<?>) params.get(i);
            switch ((String) ops.get(i)) {
                case "TopKSum" -> { s = new TopKSum(num(a, 0)); out.add(null); }
                case "upsert" -> { s.upsert(num(a, 0), num(a, 1)); out.add(null); }
                case "remove" -> { s.remove(num(a, 0)); out.add(null); }
                case "topKSum" -> out.add(s.topKSum());
                default -> throw new IllegalArgumentException("Unknown operation " + ops.get(i));
            }
        }
        IO.print(out);
    }

    static int num(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }
}
