A teammate stores binary trees as **nested maps** instead of node objects. Every node is a `Map<String, Object>` with:

- `"val"` → the node's value (an `Integer`)
- `"left"` → the left child's map, or absent/`null` if there is no left child
- `"right"` → the right child's map, or absent/`null` if there is no right child

An empty tree is `null`. They wrote `inorderTraversal`, which should return the values in **inorder** (left subtree, then the node, then the right subtree), but it gives wrong answers on some trees. The starter code is their implementation — **find the bug and fix it** (or rewrite the method) so it returns the correct inorder sequence for every tree.

Trees in the examples are shown in level order (`null` = missing child); the test harness turns them into nested maps for you.

**Example 1**
Input: root = [1,null,2,3]
Output: [1,3,2]
Why: 1 has no left subtree, so it comes first; then 2's left child 3, then 2.

**Example 2**
Input: root = [4,2,6,1,3,5,7]
Output: [1,2,3,4,5,6,7]

**Example 3**
Input: root = []
Output: []

**Constraints**
- 0 ≤ number of nodes ≤ 10,000
- −10⁵ ≤ val ≤ 10⁵
- the tree may be very deep (a single long chain)

**Notes**: the buggy version happens to pass on some shapes (try a tree where every node has only a right child) — that is why it slipped through review. Think about which traversal it actually computes.
