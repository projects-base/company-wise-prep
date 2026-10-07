import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argsList = in.nextList();
        List<Object> out = new ArrayList<>();
        MovingAverage obj = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argsList.get(i);
            switch (op) {
                case "MovingAverage" -> {
                    obj = new MovingAverage(((Number) a.get(0)).intValue());
                    out.add(null);
                }
                case "next" -> out.add(obj.next(((Number) a.get(0)).intValue()));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
