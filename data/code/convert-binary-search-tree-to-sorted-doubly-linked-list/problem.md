Given the root of a binary search tree, rearrange its nodes **in place** into a sorted, **circular**, doubly linked list and return the node with the smallest value (the head).

Reuse the tree pointers as list pointers: `left` becomes "previous" and `right` becomes "next". In the result, following `right` from the head visits the values in increasing order, the largest node's `right` points back to the head, and the head's `left` points to the largest node. Do not create new nodes. For an empty tree, return `null`.

**Example 1**
Input: root = [4,2,5,1,3]
Output: [1,2,3,4,5]
Why: the list is 1 ⇄ 2 ⇄ 3 ⇄ 4 ⇄ 5, with 5 → 1 via `right` and 1 → 5 via `left` closing the circle.

**Example 2**
Input: root = [2,1,3]
Output: [1,2,3]

**Example 3**
Input: root = []
Output: []

**Constraints**
- 0 ≤ number of nodes ≤ 2000
- −1000 ≤ node value ≤ 1000, all values distinct
- the tree is a valid binary search tree

**Notes**: the tree is given in level order with `null` for missing children. The checker walks your list from the returned head for exactly n steps and prints the values. It prints an `invalid: …` message instead if a `left` pointer does not mirror the matching `right` pointer, the list does not return to the head after n steps, or it contains nodes that were not in the original tree. Aim for O(n) time and O(h) extra space (h = tree height).
