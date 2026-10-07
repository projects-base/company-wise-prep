Every node of a binary tree holds `0` (water) or `1` (land). Two land nodes are connected when one is the parent of the other. An **island** is a maximal group of land nodes that are connected to each other through parent–child edges, using only land nodes along the way. Return the number of islands.

Trees are given in level order, with `null` for a missing child.

**Example 1**
Input: root = [1,1,0,0,1,1,1]
Output: 3
Why: the root, its left child and that child's right child form one island. The root's right child is water, so its two land children are islands on their own.

**Example 2**
Input: root = [0,1,1,null,1,1]
Output: 2
Why: the root is water. Its left child joins its own right child, and its right child joins its own left child — two separate islands.

**Example 3**
Input: root = [0]
Output: 0

**Constraints**
- 0 ≤ number of nodes ≤ 10⁵
- every node value is 0 or 1
- the tree may be very deep (a long chain)

**Notes**: an empty tree has no islands.
