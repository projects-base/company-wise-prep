import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        TreeNode root = in.nextTree();
        IO.print(new Solution().inorderTraversal(toMap(root)));
    }

    // Converts the tree into nested maps without recursion (the tree may be a long chain).
    static Map<String, Object> toMap(TreeNode root) {
        if (root == null) return null;
        Map<TreeNode, Map<String, Object>> made = new IdentityHashMap<>();
        ArrayDeque<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            Map<String, Object> m = new HashMap<>();
            m.put("val", n.val);
            made.put(n, m);
            if (n.left != null) q.add(n.left);
            if (n.right != null) q.add(n.right);
        }
        for (Map.Entry<TreeNode, Map<String, Object>> e : made.entrySet()) {
            TreeNode n = e.getKey();
            if (n.left != null) e.getValue().put("left", made.get(n.left));
            if (n.right != null) e.getValue().put("right", made.get(n.right));
        }
        return made.get(root);
    }
}
