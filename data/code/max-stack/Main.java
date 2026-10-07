import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        MaxStack st = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "MaxStack" -> { st = new MaxStack(); out.add(null); }
                case "push" -> { st.push(((Number) a.get(0)).intValue()); out.add(null); }
                case "pop" -> out.add(st.pop());
                case "top" -> out.add(st.top());
                case "peekMax" -> out.add(st.peekMax());
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
