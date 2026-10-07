Given the root of a binary tree, return its values level by level, alternating direction: the root level is read left to right, the next level right to left, the one after that left to right again, and so on. Each level is its own list.

**Example 1**
Input: root = [3,9,20,null,null,15,7]
Output: [[3],[20,9],[15,7]]

**Example 2**
Input: root = [1]
Output: [[1]]

**Example 3**
Input: root = []
Output: []

**Constraints**
- 0 ≤ number of nodes ≤ 2000
- −100 ≤ node value ≤ 100

**Notes**: the tree is given in level order with `null` for missing children. An empty tree gives an empty list.
