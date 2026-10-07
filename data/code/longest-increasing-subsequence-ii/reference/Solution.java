import java.util.*;

class Solution {
    // Segment tree over values: tree holds, for each value v, the longest valid subsequence ending in v.
    private int size;
    private int[] tree;

    public int lengthOfLIS(int[] nums, int k) {
        int maxV = 0;
        for (int x : nums) maxV = Math.max(maxV, x);
        size = 1;
        while (size <= maxV) size <<= 1;
        tree = new int[2 * size];
        int best = 0;
        for (int v : nums) {
            int len = query(Math.max(0, v - k), v - 1) + 1;
            update(v, len);
            best = Math.max(best, len);
        }
        return best;
    }

    private void update(int pos, int val) {
        int i = pos + size;
        if (tree[i] >= val) return;
        tree[i] = val;
        for (i >>= 1; i >= 1; i >>= 1) tree[i] = Math.max(tree[2 * i], tree[2 * i + 1]);
    }

    // max over values in [lo, hi]
    private int query(int lo, int hi) {
        int res = 0;
        if (lo > hi) return 0;
        int l = lo + size, r = hi + size + 1;
        while (l < r) {
            if ((l & 1) == 1) res = Math.max(res, tree[l++]);
            if ((r & 1) == 1) res = Math.max(res, tree[--r]);
            l >>= 1;
            r >>= 1;
        }
        return res;
    }
}
