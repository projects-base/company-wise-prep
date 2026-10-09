**Short answer:** Store in every node the size of its subtree. To find the rank of `x`, walk from the root as in a normal search. Every time you go right, everything in the left subtree plus the current node is smaller than `x`, so add `size(left) + 1`. When you find `x`, the rank is that running total plus `size(left) + 1`. Each query costs O(height) instead of the O(n) of an in-order traversal.

## Approach

- **Brute force.** In-order traversal counts nodes until it reaches `x`. Correct, but O(n) per query.
- **Key insight.** The rank of `x` is "1 + number of values smaller than `x`". On the search path, every time you turn right at node `n`, the whole left subtree of `n` and `n` itself are smaller. Every time you turn left, nothing on the skipped side is smaller. If each node knows its subtree size, you count those blocks in O(1) each.
- **Maintaining sizes.** On insert, increment `size` on every node along the path, because the new node joins each of their subtrees. Check for duplicates **first**, otherwise a duplicate insert would bump sizes without adding a node.

This is an *order-statistic tree*. The same `size` field also gives "k-th smallest" by walking down and comparing `k` with `size(left) + 1`.

## Solution

```java
class RankBST {
    private static final class Node {
        final int val;
        Node left, right;
        int size = 1; // nodes in this subtree, including itself
        Node(int val) { this.val = val; }
    }

    private Node root;

    public void insert(int x) {
        if (contains(x)) return; // sizes must only grow when a node is really added
        if (root == null) { root = new Node(x); return; }
        Node n = root;
        while (true) {
            n.size++;
            if (x < n.val) {
                if (n.left == null) { n.left = new Node(x); return; }
                n = n.left;
            } else {
                if (n.right == null) { n.right = new Node(x); return; }
                n = n.right;
            }
        }
    }

    public int rank(int x) {
        int smaller = 0;
        Node n = root;
        while (n != null) {
            if (x < n.val) {
                n = n.left;
            } else if (x > n.val) {
                smaller += size(n.left) + 1;
                n = n.right;
            } else {
                return smaller + size(n.left) + 1;
            }
        }
        return -1; // not in the tree
    }

    private boolean contains(int x) {
        Node n = root;
        while (n != null) {
            if (x == n.val) return true;
            n = x < n.val ? n.left : n.right;
        }
        return false;
    }

    private static int size(Node n) { return n == null ? 0 : n.size; }
}
```

## Complexity

- **Time:** O(h) for `insert` and `rank`, where h is the height. That is O(log n) for random insertion order and O(n) for sorted input.
- **Space:** one extra `int` per node; O(1) extra per operation (iterative, no recursion).

## Edge cases

- Empty tree: `rank` returns -1.
- Value not present: the walk falls off the tree, return -1.
- Duplicate insert: ignored, sizes unchanged.
- Smallest value: rank 1. Largest: rank n.

## Variations

- **Guaranteed O(log n):** add the size field to a self-balancing tree (AVL, red-black, treap). Rotations must recompute `size` for the two nodes they move: `size = size(left) + size(right) + 1`.
- **Delete:** decrement sizes along the path, only if the node exists.
- **Rank of a value not in the tree** (count of smaller values): same walk without the "not found" return.
- **Without a custom tree:** a Fenwick tree over compressed values answers rank in O(log n) when all values are known up front.

Practise it in the app: Run / Submit on this page.
