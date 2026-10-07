import java.util.*;

class Solution {
    // Each node is a map {"val": Integer, "left": Map or absent, "right": Map or absent}; root may be null.
    // This implementation has a bug: find it and fix it so the result is the inorder sequence.
    public List<Integer> inorderTraversal(Map<String, Object> root) {
        List<Integer> out = new ArrayList<>();
        Deque<Map<String, Object>> stack = new ArrayDeque<>();
        if (root != null) stack.push(root);
        while (!stack.isEmpty()) {
            Map<String, Object> node = stack.pop();
            out.add((Integer) node.get("val"));
            Map<String, Object> left = child(node, "left");
            Map<String, Object> right = child(node, "right");
            if (right != null) stack.push(right);
            if (left != null) stack.push(left);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> node, String side) {
        return (Map<String, Object>) node.get(side);
    }
}
