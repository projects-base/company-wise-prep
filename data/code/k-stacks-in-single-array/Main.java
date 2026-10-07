import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        KStacks st = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "KStacks" -> { st = new KStacks(arg(a, 0), arg(a, 1)); out.add(null); }
                case "push" -> out.add(st.push(arg(a, 0), arg(a, 1)));
                case "pop" -> out.add(st.pop(arg(a, 0)));
                case "peek" -> out.add(st.peek(arg(a, 0)));
                case "isEmpty" -> out.add(st.isEmpty(arg(a, 0)));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    private static int arg(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }
}
