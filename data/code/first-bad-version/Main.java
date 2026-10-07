import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        VersionControl.n = n;
        VersionControl.firstBad = in.nextInt();
        Object out;
        try {
            int ans = new Solution().firstBadVersion(n);
            out = VersionControl.calls > VersionControl.LIMIT
                    ? "too many isBadVersion calls: " + VersionControl.calls + " (limit " + VersionControl.LIMIT + ")"
                    : ans;
        } catch (VersionControl.TooManyCalls e) {
            out = "too many isBadVersion calls (stopped after " + VersionControl.calls + ")";
        }
        IO.print(out);
    }
}

/** The API the learner calls. The harness sets the hidden first bad version. */
class VersionControl {
    static final int LIMIT = 40;
    static int n, firstBad, calls;

    static class TooManyCalls extends RuntimeException {
    }

    boolean isBadVersion(int version) {
        if (++calls > 10_000) throw new TooManyCalls();
        if (version < 1 || version > n) throw new IllegalArgumentException("isBadVersion(" + version + ") is outside 1.." + n);
        return version >= firstBad;
    }
}
