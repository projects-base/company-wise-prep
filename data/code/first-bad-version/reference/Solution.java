import java.util.*;

class Solution extends VersionControl {
    // Binary search for the boundary; lo + (hi - lo) / 2 never overflows.
    public int firstBadVersion(int n) {
        int lo = 1, hi = n;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (isBadVersion(mid)) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }
}
