**Short answer:** The buggy code pops a node, emits it immediately, then pushes right and left children. That is a **preorder** traversal (node, left, right), not inorder. It passes on right-only chains because there preorder and inorder coincide. The fix: walk left pushing nodes onto a stack, pop and emit a node only after its whole left subtree is done, then move to its right child. Keep it iterative, since the tree can be a 10,000-node chain.

## Approach

**Find the bug first.** The starter loop is:

```java
Map<String, Object> node = stack.pop();
out.add((Integer) node.get("val"));     // emitted before its left subtree
if (right != null) stack.push(right);
if (left != null) stack.push(left);     // left popped next
```

On `[1,null,2,3]` it outputs `[1,2,3]`; the correct inorder is `[1,3,2]`. For a node with no left children (a right-only chain), "node then right" is the same in both orders, which is why it slipped through review.

**Key insight.** Inorder must delay emitting a node until its left subtree is finished. The stack has to remember nodes we have passed on the way down-left.

**Fix.** Classic iterative inorder: push the whole left spine, pop one node, emit it, move to its right child, repeat. Recursion would also be correct, but a 10,000-deep chain risks a `StackOverflowError` with the default thread stack, so the explicit `ArrayDeque` is safer.

## Solution

```java
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
```

`node.get(side)` returns `null` both when the key is absent and when it maps to `null`, so both "no child" encodings are handled.

## Complexity

- **Time:** O(n) — each node is pushed and popped once.
- **Space:** O(h) for the stack, where h is the tree height (up to n for a chain).

## Edge cases

- Empty tree (`root == null`) → `[]`; the outer loop never runs.
- Left-only chain: the stack grows to n, still fine with `ArrayDeque`.
- Missing vs explicit-null children: both read as `null`.

## Variations

- **Morris traversal:** O(1) extra space by temporarily threading right pointers to inorder successors. It mutates the tree during the walk, which is awkward with shared maps.
- **Interview habit for debugging rounds:** run the code by hand on a small asymmetric tree (one with a left child under a right child), state which traversal it really computes, then fix the smallest thing. Say why tests missed it. See [H3 · Debugging and production habits](../academy/lessons/H3.md).

Practise it in the app: Run / Submit on this page.
