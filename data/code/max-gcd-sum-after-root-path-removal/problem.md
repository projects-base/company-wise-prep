A tree has `n` nodes numbered `0` to `n − 1` and is rooted at node `0`. `parent[i]` is the parent of node `i` (`parent[0] = −1`), and `values[i]` is the positive value of node `i`.

Choose any node `v` (the root itself is allowed) and delete every node on the path from the root down to `v`, including both ends. The remaining nodes split into connected components; each one is a complete subtree that hung below a deleted node. The **score** is the sum, over all remaining components, of the GCD of the values in that component. If nothing remains, the score is 0.

Return the maximum score over all choices of `v`.

**Example 1**
Input: parent = [-1,0,0,1,1], values = [6,4,9,2,8]
Output: 19
Why: node 0 has children 1 and 2, and node 1 has children 3 and 4. Choosing v = 1 deletes nodes 0 and 1 and leaves {2}, {3}, {4} with GCDs 9 + 2 + 8 = 19. Choosing v = 0 leaves {1,3,4} (GCD 2) and {2} (GCD 9), which scores only 11.

**Example 2**
Input: parent = [-1], values = [7]
Output: 0
Why: deleting the root leaves nothing.

**Example 3**
Input: parent = [-1,0,1,2], values = [5,10,15,20]
Output: 20
Why: on this chain, choosing v = 2 leaves only {3}, with GCD 20. Choosing v = 0 leaves {1,2,3}, with GCD 5.

**Constraints**
- 1 ≤ n ≤ 2·10⁵
- parent describes a valid tree rooted at 0 (a parent's number may be larger than its child's)
- 1 ≤ values[i] ≤ 10⁹

**Notes**: the original report asks for the result modulo 10⁹ + 7. The true maximum is at most 2·10⁵ · 10⁹ and fits in a `long`, so return it exactly, without the modulo. Trying every v and rebuilding the components costs O(n²). Hidden tests include a chain of 20,000 nodes, so recursion depth matters; an iterative traversal is safest.
