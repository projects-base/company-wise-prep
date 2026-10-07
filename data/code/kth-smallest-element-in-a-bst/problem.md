Given the `root` of a binary search tree (every value in a node's left subtree is smaller than the node's value, and every value in its right subtree is larger) and an integer `k`, return the **k-th smallest** value in the tree, counting from 1.

Trees are given in level order, with `null` for a missing child.

**Example 1**
Input: root = [3,1,4,null,2], k = 1
Output: 1

**Example 2**
Input: root = [5,3,6,2,4,null,null,1], k = 3
Output: 3
Why: the values in increasing order are 1, 2, 3, 4, 5, 6.

**Example 3**
Input: root = [2,1,3], k = 3
Output: 3

**Constraints**
- 1 ≤ k ≤ number of nodes ≤ 10⁴
- 0 ≤ node value ≤ 10⁴, all values distinct
- the tree may be very unbalanced

**Notes**: an inorder walk visits a BST's values in increasing order; you can stop as soon as you reach the k-th one.
