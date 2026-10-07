import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        TreeNode root = in.nextTree();
        // Remember the original nodes so we can check the list reuses them.
        Set<TreeNode> original = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<TreeNode> stack = new ArrayDeque<>();
        if (root != null) stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode t = stack.pop();
            original.add(t);
            if (t.left != null) stack.push(t.left);
            if (t.right != null) stack.push(t.right);
        }
        int n = original.size();

        TreeNode head = new Solution().treeToDoublyList(root);
        if (n == 0) {
            IO.print(head == null ? new ArrayList<Integer>() : "invalid: expected null for an empty tree");
            return;
        }
        IO.print(walk(head, n, original));
    }

    private static Object walk(TreeNode head, int n, Set<TreeNode> original) {
        if (head == null) return "invalid: returned null for a non-empty tree";
        List<Integer> vals = new ArrayList<>();
        Set<TreeNode> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        TreeNode cur = head;
        for (int i = 0; i < n; i++) {
            if (cur == null) return "invalid: list ends after " + i + " nodes (it must be circular)";
            if (!original.contains(cur)) return "invalid: node " + cur.val + " is not one of the original tree nodes";
            if (!seen.add(cur)) return "invalid: node " + cur.val + " is visited twice before the list closes";
            if (cur.right == null || cur.right.left != cur) return "invalid: left/right pointers do not mirror each other at node " + cur.val;
            vals.add(cur.val);
            cur = cur.right;
        }
        if (cur != head) return "invalid: after " + n + " steps the list does not return to the head";
        return vals;
    }
}
