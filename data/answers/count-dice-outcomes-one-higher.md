**Short answer:** For two dice, every outcome except a tie works: n² − n = n(n − 1). For k dice, fix the value v of the unique maximum. Choose which die shows it (k ways); every other die must show something in 1..v−1, giving (v − 1)^(k−1) ways. The answer is k · Σ_{v=2..n} (v − 1)^(k−1) mod 1e9+7, computed with fast power in O(n log k).

## Approach

**Brute force.** Enumerate all nᵏ outcomes and check whether the maximum is unique. Exponential; useless for k = 10⁹.

**Key insight.** Classify each good outcome by its maximum value v. The maximum appears on exactly one die, so:

- pick the die that shows v: k choices;
- each of the other k − 1 dice shows a value strictly below v: v − 1 choices each.

Different v give disjoint sets of outcomes, so the counts simply add. v = 1 contributes 0 (nothing is below 1), so start at v = 2.

**Optimal.** Loop v from 2 to n, add `pow(v − 1, k − 1)` with binary exponentiation, and multiply the total by k at the end.

For k = 2 this collapses to 2 · Σ (v − 1) = 2 · n(n − 1)/2 = n(n − 1), matching "all outcomes minus the n ties".

## Solution

```java
import java.util.*;

class Solution {
    private static final long MOD = 1_000_000_007L;

    // If the unique maximum is v, choose which die shows it (k ways); every other die shows
    // one of 1..v-1, giving (v-1)^(k-1) outcomes. Sum over v.
    public int countOutcomes(int n, int k) {
        long total = 0;
        for (long v = 2; v <= n; v++) {
            total = (total + pow(v - 1, k - 1)) % MOD;
        }
        return (int) (total * (k % MOD) % MOD);
    }

    private static long pow(long b, long e) {
        long r = 1;
        b %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) r = r * b % MOD;
            b = b * b % MOD;
            e >>= 1;
        }
        return r;
    }
}
```

Check n = 3, k = 3: v = 2 gives 1² = 1, v = 3 gives 2² = 4; (1 + 4) · 3 = 15.

## Complexity

- **Time:** O(n log k) — n terms, each a fast power of about 30 squarings for k ≤ 10⁹.
- **Space:** O(1).

## Edge cases

- n = 1 → 0: every die shows 1, so the maximum is always shared.
- k = 2 → n(n − 1); n = 6 gives 30.
- Overflow: keep values reduced below MOD, so every product is below 10¹⁸ and fits in a `long`.

## Follow-ups

- **Generalise efficiently to k dice.** That is the formula above, O(n log k). If n were also huge (10¹⁸), the sum Σ_{j=1..n−1} j^(k−1) is a power sum, a polynomial in n of degree k; Faulhaber's formula or Lagrange interpolation evaluates it in about O(k log MOD), which only helps when k is small. With n ≤ 10⁶ and k ≤ 10⁹, the O(n log k) loop is the right target.
- **Probability instead of a count:** divide by nᵏ, i.e. multiply by the modular inverse `pow(pow(n, k), MOD − 2)` (Fermat, since MOD is prime).
- **Ambiguity worth raising:** for k dice, "one die beats another" could also mean "not all dice equal", which is nᵏ − n. Confirm which meaning the interviewer wants.

See also [C1 · From constraints to the expected complexity](../academy/lessons/C1.md).

Practise it in the app: Run / Submit on this page.
