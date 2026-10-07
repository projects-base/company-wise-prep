import java.util.*;

class Codec {
    // Format: "n|v0|v1|...|e0|e1|..." where node i is the i-th node met in a BFS from the root
    // and ei is the comma-separated list of its neighbours' numbers, in order.
    public String serialize(Node root) {
        if (root == null) return "0";
        Map<Node, Integer> id = new IdentityHashMap<>();
        List<Node> order = new ArrayList<>();
        id.put(root, 0);
        order.add(root);
        for (int k = 0; k < order.size(); k++) {
            for (Node m : order.get(k).neighbors) {
                if (!id.containsKey(m)) {
                    id.put(m, order.size());
                    order.add(m);
                }
            }
        }
        StringBuilder sb = new StringBuilder().append(order.size());
        for (Node n : order) sb.append('|').append(n.val);
        for (Node n : order) {
            sb.append('|');
            for (int i = 0; i < n.neighbors.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(id.get(n.neighbors.get(i)));
            }
        }
        return sb.toString();
    }

    public Node deserialize(String data) {
        String[] parts = data.split("\\|", -1);
        int n = Integer.parseInt(parts[0]);
        if (n == 0) return null;
        Node[] nodes = new Node[n];
        for (int i = 0; i < n; i++) nodes[i] = new Node(Integer.parseInt(parts[1 + i]));
        for (int i = 0; i < n; i++) {
            String edges = parts[1 + n + i];
            if (edges.isEmpty()) continue;
            for (String e : edges.split(",")) nodes[i].neighbors.add(nodes[Integer.parseInt(e)]);
        }
        return nodes[0];
    }
}
