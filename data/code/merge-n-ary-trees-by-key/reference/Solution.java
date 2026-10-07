import java.util.*;

class Solution {
    public Node merge(Node a, Node b) {
        if (a == null) return b;
        if (b == null) return a;
        Node out = new Node(a.key, b.value);
        Map<String, Node> bByKey = new HashMap<>();
        for (Node c : b.children) bByKey.put(c.key, c);
        Set<String> usedFromB = new HashSet<>();
        for (Node c : a.children) {
            Node match = bByKey.get(c.key);
            if (match != null) {
                usedFromB.add(c.key);
                out.children.add(merge(c, match));
            } else {
                out.children.add(c);
            }
        }
        for (Node c : b.children) {
            if (!usedFromB.contains(c.key)) out.children.add(c);
        }
        return out;
    }
}
