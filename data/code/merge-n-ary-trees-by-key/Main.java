import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        Node a = build(in.nextValue());
        Node b = build(in.nextValue());
        Node merged = new Solution().merge(a, b);
        IO.print(toList(merged));
    }

    // [key, value, [children...]] or null
    static Node build(Object v) {
        if (v == null) return null;
        List<?> l = (List<?>) v;
        Node n = new Node((String) l.get(0), ((Number) l.get(1)).intValue());
        for (Object c : (List<?>) l.get(2)) n.children.add(build(c));
        return n;
    }

    static Object toList(Node n) {
        if (n == null) return null;
        List<Object> kids = new ArrayList<>();
        if (n.children != null) for (Node c : n.children) kids.add(toList(c));
        List<Object> out = new ArrayList<>();
        out.add(n.key);
        out.add(n.value);
        out.add(kids);
        return out;
    }
}

/** An n-ary tree node with a key and a value. */
class Node {
    String key;
    int value;
    List<Node> children = new ArrayList<>();

    Node(String key, int value) {
        this.key = key;
        this.value = value;
    }
}
