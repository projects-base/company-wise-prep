A path in a binary tree is a sequence of nodes where each consecutive pair is joined by an edge, and no node appears more than once. A path has at least one node and does not have to pass through the root. The sum of a path is the sum of its node values. Given the `root` of a non-empty binary tree, return the largest sum of any path.

**Example 1**
Input: root = [1,2,3]
Output: 6
Why: the path 2 → 1 → 3 has sum 6.

**Example 2**
Input: root = [-10,9,20,null,null,15,7]
Output: 42
Why: the path 15 → 20 → 7 has sum 42; including -10 would only lower it.

**Example 3**
Input: root = [-3]
Output: -3
Why: a path must contain at least one node, even if every value is negative.

**Constraints**
- 1 ≤ number of nodes ≤ 3 · 10⁴
- −1000 ≤ Node.val ≤ 1000
- the tree is given in level order, with `null` for missing children

**Notes**: the hidden tests include a tree with 12,000 nodes, so an O(n²) approach will be slow.
