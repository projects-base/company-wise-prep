import java.util.*;

class Solution {
    // Each island has exactly one "top" node: a land node whose parent is missing or water.
    // Count those, walking the tree with an explicit stack (it may be very deep).
    public int countIslands(TreeNode root) {
        if (root == null) return 0;
        int islands = 0;
        Deque<TreeNode[]> stack = new ArrayDeque<>(); // {node, parent}
        stack.push(new TreeNode[] {root, null});
        while (!stack.isEmpty()) {
            TreeNode[] top = stack.pop();
            TreeNode node = top[0], parent = top[1];
            if (node.val == 1 && (parent == null || parent.val == 0)) islands++;
            if (node.left != null) stack.push(new TreeNode[] {node.left, node});
            if (node.right != null) stack.push(new TreeNode[] {node.right, node});
        }
        return islands;
    }
}
