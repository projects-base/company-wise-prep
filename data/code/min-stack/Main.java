import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        MinStack st = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "MinStack" -> { st = new MinStack(); out.add(null); }
                case "push" -> { st.push(((Number) a.get(0)).intValue()); out.add(null); }
                case "pop" -> { st.pop(); out.add(null); }
                case "top" -> out.add(st.top());
                case "getMin" -> out.add(st.getMin());
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
