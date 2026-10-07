import java.util.*;

class Node {
    int val;
    List<Node> neighbors = new ArrayList<>();

    Node(int val) {
        this.val = val;
    }
}

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] values = in.nextIntArray();
        int[][] adjacency = in.nextIntMatrix();
        Node[] nodes = new Node[values.length];
        for (int i = 0; i < nodes.length; i++) nodes[i] = new Node(values[i]);
        for (int i = 0; i < nodes.length; i++) for (int j : adjacency[i]) nodes[i].neighbors.add(nodes[j]);
        Node root = nodes.length == 0 ? null : nodes[0];

        String data = new Codec().serialize(root);
        // Scramble the original so the copy can only come from the string.
        Set<Node> originals = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node n : nodes) {
            originals.add(n);
            n.val = Integer.MIN_VALUE;
            n.neighbors = new ArrayList<>();
        }
        Node copy = new Codec().deserialize(data);
        IO.print(describe(copy, originals));
    }

    /** Breadth-first walk from root, numbering nodes by first visit. */
    static Object describe(Node root, Set<Node> originals) {
        List<Object> out = new ArrayList<>();
        if (root == null) return out;
        Map<Node, Integer> id = new IdentityHashMap<>();
        List<Node> order = new ArrayList<>();
        id.put(root, 0);
        order.add(root);
        for (int k = 0; k < order.size(); k++) {
            Node n = order.get(k);
            if (originals.contains(n)) return "deserialize returned an original node instead of a new one";
            List<Integer> nb = new ArrayList<>();
            for (Node m : n.neighbors) {
                if (m == null) return "a neighbors list contains null";
                Integer x = id.get(m);
                if (x == null) {
                    x = order.size();
                    id.put(m, x);
                    order.add(m);
                }
                nb.add(x);
            }
            out.add(List.of(n.val, nb));
        }
        return out;
    }
}
