**Short answer:** Binary search on the answer. The hours needed, `sum(ceil(pile / k))`, only goes down as speed k goes up, so "can finish in h hours" is false for small k and true from some point on. Search k in `[1, max(pile)]` for the first speed that works. Each check is O(n), so the total is O(n log max).

## Picture it

Example 1: `piles = [3, 6, 7, 11]`, `h = 8`. Search speeds in `[1, 11]`.

| Step | lo | hi | mid | hours = Σ ceil(pile / mid) | ≤ 8? | Action |
|---|---|---|---|---|---|---|
| 1 | 1 | 11 | 6 | 1 + 1 + 2 + 2 = 6 | yes | hi = 6 |
| 2 | 1 | 6 | 3 | 1 + 2 + 3 + 4 = 10 | no | lo = 4 |
| 3 | 4 | 6 | 5 | 1 + 2 + 2 + 3 = 8 | yes | hi = 5 |
| 4 | 4 | 5 | 4 | 1 + 2 + 2 + 3 = 8 | yes | hi = 4 |
| 5 | 4 | 4 | — | lo == hi | — | return 4 |

```text
speed:     1  2  3 | 4  5  6  ...  11
fits h=8?  no no no| yes yes yes  yes     <- one boundary, binary search finds it
```

**The picture in one sentence:** "fast enough" flips from no to yes exactly once as speed grows, so binary search the speed instead of trying them all.

## Approach

- **Brute force:** try k = 1, 2, 3, … and stop at the first speed that finishes in time. Up to 10⁹ speeds × 10⁴ piles: far too slow.
- **Key insight:** the predicate `hours(k) <= h` is monotonic. If speed k is enough, every faster speed is too. Monotonic yes/no over a range = binary search for the boundary.
- **Bounds:** k = 1 is the slowest sensible speed. k = max(pile) always works, because then every pile takes exactly one hour and h ≥ number of piles. Going faster than the biggest pile never helps.

## Solution

```java
class Solution {
    public int minEatingSpeed(int[] piles, int h) {
        int lo = 1, hi = 1;
        for (int p : piles) hi = Math.max(hi, p);
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            long hours = 0;                               // can exceed int
            for (int p : piles) hours += (p + (long) mid - 1) / mid;  // ceil(p / mid)
            if (hours <= h) hi = mid;   // mid works: answer is mid or slower
            else lo = mid + 1;          // too slow
        }
        return lo;
    }
}
```

## Complexity

- **Time:** O(n · log M), where M is the largest pile (about 30 iterations for 10⁹).
- **Space:** O(1).

## Edge cases

- `h == piles.length`: one hour per pile, so the answer is the largest pile.
- Very large h: the answer can be 1.
- Overflow, twice: the total hours can exceed `Integer.MAX_VALUE` at speed 1 (10⁴ piles × 10⁹), so sum into a `long`. And `p + mid - 1` can overflow `int` when both are near 10⁹, so do it in `long`.
- `mid = lo + (hi - lo) / 2` avoids overflow of `lo + hi`.
- Single pile: answer is `ceil(pile / h)`.

## Variations

The same "binary search on the answer" pattern solves:

- Capacity to Ship Packages Within D Days (search the ship capacity).
- Split Array Largest Sum (search the largest allowed sum).
- Minimum Number of Days to Make m Bouquets (search the day).
- Minimize Max Distance to Gas Station (binary search on a real number).

The recipe: find a quantity where "is X enough?" is monotonic, write the O(n) check, and search for the first X that passes.

See [C1 · From constraints to the expected complexity](../academy/lessons/C1.md): values up to 10⁹ with n up to 10⁴ point straight at a log factor on the value.

Practise it in the app: Run / Submit on this page.
