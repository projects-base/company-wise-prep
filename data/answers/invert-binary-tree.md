**Short answer:** To mirror a tree, swap the left and right child of every node. Recursively: mirror the left subtree, mirror the right subtree, then swap them (or swap first and recurse, both work). Every node is visited once, so it is O(n) time and O(h) stack space, where `h` is the height. An iterative BFS with a queue does the same without recursion.

## Approach

**Key insight.** The mirror of a tree is the root with its *mirrored* right subtree on the left and its *mirrored* left subtree on the right. That definition is already the recursion.

**Example.**

```text
      4                 4
    /   \             /   \
   2     7    ->     7     2
  / \   / \         / \   / \
 1   3 6   9       9   6 3   1
```

**Iterative alternative.** Put the root in a queue. Poll a node, swap its children, enqueue the non-null ones. Order does not matter, since each node only needs its own children swapped once. This avoids stack overflow on very deep (skewed) trees.

## Solution

```java
import java.util.ArrayDeque;
import java.util.Deque;

class Solution {
    // Recursive: O(n) time, O(h) stack.
    public TreeNode invertTree(TreeNode root) {
        if (root == null) return null;
        TreeNode left = invertTree(root.left);
        TreeNode right = invertTree(root.right);
        root.left = right;
        root.right = left;
        return root;
    }

    // Iterative BFS: same result, no recursion depth limit.
    public TreeNode invertTreeIterative(TreeNode root) {
        if (root == null) return null;
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            TreeNode tmp = node.left;
            node.left = node.right;
            node.right = tmp;
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
        return root;
    }
}
```

Note the recursive version saves both results in locals before assigning. Writing `root.left = invertTree(root.right); root.right = invertTree(root.left);` is a classic bug: the second call sees the *new* `root.left`.

## Complexity

- **Time:** O(n). Each node is visited once and the swap is O(1).
- **Space:** O(h) for the recursion stack: O(log n) for a balanced tree, O(n) for a skewed one. The BFS version uses O(w), the widest level, up to about n/2.

## Edge cases

- Empty tree: return `null`.
- Single node: unchanged.
- Skewed tree (a "linked list" of left children): becomes a chain of right children. With around 10⁵ nodes, recursion can throw `StackOverflowError`; use the iterative version.
- `ArrayDeque` rejects `null`, so enqueue only non-null children.

## Variations

- **Mirror into a new tree** (keep the original): build `new TreeNode(root.val, mirror(root.right), mirror(root.left))`.
- **Is a tree symmetric?** Compare `left` with `right` mirrored: `isMirror(a, b)` checks `a.val == b.val`, `isMirror(a.left, b.right)` and `isMirror(a.right, b.left)`.
- **Are two trees mirrors of each other?** Same `isMirror` on the two roots.
- **Check your result:** the inorder traversal of the mirrored tree is the reverse of the original's inorder traversal. Mention this as a quick way to test the function in your own `main`.
- **Pre-order vs post-order swap:** swapping before recursing (pre-order) or after (post-order) both work. Swapping in-order (between the two recursive calls) is a trap: the subtree you just mirrored moves to the other side and gets mirrored again.

See [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).
