import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        MinHeap heap = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "MinHeap" -> { heap = new MinHeap(); out.add(null); }
                case "offer" -> { heap.offer(((Number) a.get(0)).intValue()); out.add(null); }
                case "poll" -> out.add(heap.poll());
                case "peek" -> out.add(heap.peek());
                case "size" -> out.add(heap.size());
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
