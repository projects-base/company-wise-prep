import java.util.*;

class Solution {
    public int amountOfTime(TreeNode root, int start) {
        // Record each node's parent, then BFS outward from the start node.
        Map<TreeNode, TreeNode> parent = new HashMap<>();
        TreeNode src = null;
        ArrayDeque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode n = stack.pop();
            if (n.val == start) src = n;
            if (n.left != null) { parent.put(n.left, n); stack.push(n.left); }
            if (n.right != null) { parent.put(n.right, n); stack.push(n.right); }
        }
        Set<TreeNode> seen = new HashSet<>();
        ArrayDeque<TreeNode> q = new ArrayDeque<>();
        q.add(src);
        seen.add(src);
        int minutes = -1;
        while (!q.isEmpty()) {
            minutes++;
            for (int s = q.size(); s > 0; s--) {
                TreeNode n = q.poll();
                for (TreeNode m : new TreeNode[] {n.left, n.right, parent.get(n)}) {
                    if (m != null && seen.add(m)) q.add(m);
                }
            }
        }
        return minutes;
    }
}
