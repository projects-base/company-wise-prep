import java.util.*;

class Solution {
    // There are 2^k strings of length k, and those of length k start at index 2^k - 1.
    // So encode(num) is the binary form of num + 1 with its leading 1 removed.
    public String encode(int num) {
        return Long.toBinaryString((long) num + 1).substring(1);
    }
}
