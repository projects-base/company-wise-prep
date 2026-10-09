**Short answer:** Use two pointers over cube roots: `a` starts at 1, `b` starts at the largest integer with `b³ < n`. If `a³ + b³` is too small, increase `a`; too big, decrease `b`; equal, count a way and move both. Stop when `a > b`, and return whether exactly two ways were found. That is O(n^(1/3)) time. The bugs to look for in the given code are floating-point cube roots, overflow, double-counting `(a, b)` and `(b, a)`, and "at least two" instead of "exactly two".

## Approach

- **Brute force.** Try every pair `a ≤ b ≤ cbrt(n)`: O(n^(2/3)), about 1.6·10¹² pairs for n = 2·10¹⁸. Far too slow.
- **One loop + cube root.** For each `a`, compute `b = round(cbrt(n - a³))` and check `b³ == n - a³`. O(n^(1/3)) but relies on floating point; it must verify with integer arithmetic and check `b ± 1`.
- **Key insight (two pointers).** As `a` grows, the matching `b` can only shrink, exactly like two-sum on a sorted array. Each step moves one pointer, so at most `cbrt(n)` ≈ 1.26·10⁶ steps.

## Solution

```java
class Solution {
    public boolean isTaxicab(long n) {
        // a grows from 1, b shrinks from the largest value with b^3 < n.
        // With n <= 2e18 both stay below 1.3e6, so a^3 + b^3 fits in a long.
        long b = (long) Math.cbrt((double) n);
        while (b > 0 && b * b * b >= n) b--;          // fix floating-point error downwards
        while ((b + 1) * (b + 1) * (b + 1) < n) b++;  // ... and upwards
        long a = 1;
        int ways = 0;
        while (a <= b) {
            long s = a * a * a + b * b * b;
            if (s == n) { ways++; a++; b--; }
            else if (s < n) a++;
            else b--;
        }
        return ways == 2;
    }
}
```

`a ≤ b` in the loop condition counts each unordered pair once; `a == b` (like 2 = 1³ + 1³) is allowed.

## Complexity

- **Time:** O(n^(1/3)), about 1.26 million iterations at the upper limit.
- **Space:** O(1).

## Edge cases

- `n = 1`: no way (needs a, b ≥ 1, so the smallest sum is 2). False.
- `n = 2`: one way (1, 1). False.
- 1729 and 4104: true.
- 87539319: three ways. False, because it must be exactly two.
- Large n near 2·10¹⁸: the cube root from `Math.cbrt` can be off by one, hence the correcting loops.

## Bugs typically planted in this kind of "fix the code" task

- `int` cubes: `int` overflows above 1290³, so 1729-scale tests pass while large ones fail. Use `long` (in C++, `long long`).
- `pow(x, 1.0/3)` or `cbrt` truncated without verification. `cbrt(64)` may return 3.9999…, truncating to 3.
- Loops over `a` and `b` independently, counting `(1,12)` and `(12,1)` as two ways.
- `ways >= 2` instead of `ways == 2`, or returning true as soon as the second way is found without checking for a third.
- Starting `a` or `b` at 0, which allows `0³ + b³`.

In the interview, fix them one at a time and run the edge cases above after each fix.

Practise it in the app: Run / Submit on this page.
