**Short answer:** Build the string one letter at a time and keep only a tiny state: does it end in `a`, and has the single `c` been used? That gives 2 × 2 = 4 counters. Each step, `b` is always allowed, `a` only if the string does not already end in `a`, and `c` only if no `c` has been used. O(k) time, O(1) space, modulus at every step.

## Approach

**Brute force.** Generate all 3ᵏ strings and check both rules. Fine for k = 10, hopeless for k = 200.

**Key insight.** Whether a new letter is legal depends on only two facts about the prefix: the last letter is `a` or not, and a `c` has appeared or not. Every prefix with the same two facts has exactly the same set of legal continuations, so we can merge them into one count.

**Optimal.** `dp[endsWithA][cUsed]` = number of valid strings of the current length. Start with the empty string in `dp[0][0] = 1`. For each extension:

- append `b` → `dp[0][c]` (no longer ends in `a`, `c` flag unchanged)
- append `a` → `dp[1][c]`, only from `endsWithA = 0`
- append `c` → `dp[0][1]`, only from `cUsed = 0`

After k steps, sum all four cells.

## Solution

```java
import java.util.*;

class Solution {
    public int countStrings(int k) {
        final long MOD = 1_000_000_007L;
        // dp[endsWithA][cUsed] = number of valid strings of the current length
        long[][] dp = new long[2][2];
        dp[0][0] = 1; // the empty string: does not end with 'a', no 'c' used
        for (int len = 0; len < k; len++) {
            long[][] nx = new long[2][2];
            for (int endA = 0; endA < 2; endA++) {
                for (int c = 0; c < 2; c++) {
                    long cur = dp[endA][c];
                    if (cur == 0) continue;
                    nx[0][c] = (nx[0][c] + cur) % MOD;                // append 'b'
                    if (endA == 0) nx[1][c] = (nx[1][c] + cur) % MOD; // append 'a'
                    if (c == 0) nx[0][1] = (nx[0][1] + cur) % MOD;    // append the single 'c'
                }
            }
            dp = nx;
        }
        return (int) ((dp[0][0] + dp[0][1] + dp[1][0] + dp[1][1]) % MOD);
    }
}
```

Check k = 2: after one step the states hold "b" in [0][0], "a" in [1][0] and "c" in [0][1]. From "b" all 3 letters work, from "a" only `b`/`c` (2), from "c" only `a`/`b` (2): 3 + 2 + 2 = 7. That is all 9 strings minus "aa" and "cc".

## Complexity

- **Time:** O(k) — k steps, each doing constant work over 4 states.
- **Space:** O(1) — two 2 × 2 arrays.

## Edge cases

- k = 1 → 3 ("a", "b", "c").
- Overflow: each cell is below MOD before an addition, so sums fit easily in a `long`. Reduce on every add, not only at the end.
- `c` resets the "ends in a" flag, so "aca" is valid.

## Variations

- **At most m `c`s:** make the second dimension 0..m. O(k·m).
- **No run of r `a`s:** track the number of trailing `a`s (0..r−1) instead of a boolean.
- **Huge k (say 10¹⁸):** the transition is a fixed 4 × 4 matrix, so use matrix exponentiation for O(log k).
- **Closed form:** count {a, b} strings with no "aa" (a Fibonacci-like sequence), then add the strings with exactly one `c` by placing it at each position, which splits the string into two independent {a, b} parts. The DP is easier to get right in an interview.

See also [C4 · The optimisation playbook](../academy/lessons/C4.md).

Practise it in the app: Run / Submit on this page.
