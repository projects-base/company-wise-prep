import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        RankBST bst = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "RankBST" -> { bst = new RankBST(); out.add(null); }
                case "insert" -> { bst.insert(((Number) a.get(0)).intValue()); out.add(null); }
                case "rank" -> out.add(bst.rank(((Number) a.get(0)).intValue()));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
