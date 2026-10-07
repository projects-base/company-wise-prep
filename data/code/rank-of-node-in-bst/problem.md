Build a binary search tree of distinct integers that can answer *rank* queries quickly. The rank of a value is its 1-based position among all values currently stored, in ascending order (the smallest value has rank 1). Implement class `RankBST`:

- `RankBST()` creates an empty tree.
- `void insert(int x)` inserts `x` with ordinary (unbalanced) BST insertion. If `x` is already stored, nothing changes.
- `int rank(int x)` returns the rank of `x`, or `-1` if `x` is not in the tree.

**Input format**: two lines — the list of operation names, then the list of argument lists. The checker prints the list of return values, with `null` for the constructor and `insert`.

**Example 1**
Input:
["RankBST","insert","insert","insert","insert","rank","rank","rank","insert","rank"]
[[],[20],[10],[30],[25],[10],[25],[15],[15],[25]]
Output: [null,null,null,null,null,1,3,-1,null,4]
Why: sorted, the tree holds 10, 20, 25, 30, so 25 is third; 15 is absent until it is inserted, after which 25 becomes fourth.

**Example 2**
Input:
["RankBST","insert","insert","rank","insert","rank"]
[[],[5],[5],[5],[1],[5]]
Output: [null,null,null,1,null,2]
Why: the second insert of 5 is ignored.

**Constraints**
- −10⁹ ≤ x ≤ 10⁹
- at most 2 · 10⁴ calls in total; values in the larger tests are inserted in random order, so the tree stays shallow

**Notes**: an in-order traversal answers a query in O(n). The intended solution stores in every node the size of its subtree (updated on insert), so `rank` walks one root-to-node path: each time you go right, add the left subtree's size plus one. That makes each query O(height).
