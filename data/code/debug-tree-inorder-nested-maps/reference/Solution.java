import java.util.*;

class Solution {
    // The original code pushed right then left and emitted each node as soon as it was popped:
    // that is a preorder traversal. Inorder must walk all the way left first and emit a node
    // only after its whole left subtree, then continue with its right subtree.
    public List<Integer> inorderTraversal(Map<String, Object> root) {
        List<Integer> out = new ArrayList<>();
        Deque<Map<String, Object>> stack = new ArrayDeque<>();
        Map<String, Object> cur = root;
        while (cur != null || !stack.isEmpty()) {
            while (cur != null) {
                stack.push(cur);
                cur = child(cur, "left");
            }
            Map<String, Object> node = stack.pop();
            out.add((Integer) node.get("val"));
            cur = child(node, "right");
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> node, String side) {
        return (Map<String, Object>) node.get(side);
    }
}
