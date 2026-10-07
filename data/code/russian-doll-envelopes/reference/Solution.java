import java.util.*;

class Solution {
    public int maxEnvelopes(int[][] envelopes) {
        // Sort by width ascending, and by height descending for equal widths so that two
        // envelopes of the same width can never both be in the increasing subsequence.
        Arrays.sort(envelopes, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(b[1], a[1]));
        int[] tails = new int[envelopes.length];
        int len = 0;
        for (int[] e : envelopes) {
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < e[1]) lo = mid + 1; else hi = mid;
            }
            tails[lo] = e[1];
            if (lo == len) len++;
        }
        return len;
    }
}
