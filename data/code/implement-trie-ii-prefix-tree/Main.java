import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> argLists = in.nextList();
        List<Object> out = new ArrayList<>();
        Trie trie = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) argLists.get(i);
            switch (op) {
                case "Trie" -> { trie = new Trie(); out.add(null); }
                case "insert" -> { trie.insert((String) a.get(0)); out.add(null); }
                case "countWordsEqualTo" -> out.add(trie.countWordsEqualTo((String) a.get(0)));
                case "countWordsStartingWith" -> out.add(trie.countWordsStartingWith((String) a.get(0)));
                case "erase" -> { trie.erase((String) a.get(0)); out.add(null); }
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
