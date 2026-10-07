import java.util.*;

class Solution {
    // A single token on node i is a Nim heap of size (n - 1 - i): moving it to j shrinks the heap to
    // any smaller size, and it is dead at the last node. Tokens are independent games, so the
    // position is the Nim sum (XOR) of all heaps; equal pairs cancel, so only odd counts matter.
    public boolean firstPlayerWins(int[] tokens) {
        int n = tokens.length, x = 0;
        for (int i = 0; i < n; i++) {
            if ((tokens[i] & 1) == 1) x ^= n - 1 - i;
        }
        return x != 0;
    }
}
