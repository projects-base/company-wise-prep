import java.util.*;

class Solution {
    public TreeNode treeToDoublyList(TreeNode root) {
        if (root == null) return null;
        TreeNode head = null, prev = null;
        // iterative in-order traversal, linking each node to the previous one
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !stack.isEmpty()) {
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();
            TreeNode next = cur.right; // read before overwriting
            if (prev == null) head = cur;
            else {
                prev.right = cur;
                cur.left = prev;
            }
            prev = cur;
            cur = next;
        }
        // close the circle
        prev.right = head;
        head.left = prev;
        return head;
    }
}
