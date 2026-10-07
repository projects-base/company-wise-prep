import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        RangeModule rm = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "RangeModule" -> { rm = new RangeModule(); out.add(null); }
                case "addRange" -> { rm.addRange(arg(a, 0), arg(a, 1)); out.add(null); }
                case "removeRange" -> { rm.removeRange(arg(a, 0), arg(a, 1)); out.add(null); }
                case "queryRange" -> out.add(rm.queryRange(arg(a, 0), arg(a, 1)));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    static int arg(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }
}
