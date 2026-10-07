import java.util.*;

class RankBST {
    private static final class Node {
        final int val;
        Node left, right;
        int size = 1; // nodes in this subtree, including itself

        Node(int val) {
            this.val = val;
        }
    }

    private Node root;

    public RankBST() {
    }

    public void insert(int x) {
        if (contains(x)) return; // sizes must only grow when a node is really added
        if (root == null) {
            root = new Node(x);
            return;
        }
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
        return -1;
    }

    private boolean contains(int x) {
        Node n = root;
        while (n != null) {
            if (x == n.val) return true;
            n = x < n.val ? n.left : n.right;
        }
        return false;
    }

    private static int size(Node n) {
        return n == null ? 0 : n.size;
    }
}
