**Short answer:** Sort the products by release time, breaking ties by alternate time, and walk through them keeping `last`, the time of the latest release so far. For each product use the smaller of its two times that is at least `last`; the final `last` is the answer, in O(n log n).

**Assumptions (the reported wording is loose, so say these first):**
- "Order of the first array" means order of **release time**: the report's example says a product with time 5 must come before one with time 6. If it means array position, see Variations.
- Two products may be released at the same moment, so the sequence must be non-decreasing.
- Products with the same release time may go in either order.
- A product uses exactly one of its two times. The alternate may be earlier or later.

## Picture it

Example A: `release = [5, 3, 4]`, `alternate = [2, 1, 2]`.

| product (release, alternate) | last before | alternate ≥ last? | chosen | last after |
|---|---|---|---|---|
| (3, 1) | none | yes | 1 | 1 |
| (4, 2) | 1 | yes | 2 | 2 |
| (5, 2) | 2 | yes, equal is fine | 2 | 2 |

Answer 2.

Example B: `release = [6, 5, 4]`, `alternate = [1, 2, 3]`.

| product (release, alternate) | last before | alternate ≥ last? | chosen | last after |
|---|---|---|---|---|
| (4, 3) | none | yes | 3 | 3 |
| (5, 2) | 3 | no | 5 | 5 |
| (6, 1) | 5 | no | 6 | 6 |

Answer 6.

**The picture in one sentence:** in the forced order, always take the earliest time you still can, because a smaller `last` never hurts a later product.

## Approach

- **Brute force:** each product uses one of two times, so try 2ⁿ choices and keep the non-decreasing ones.
- **Key insight (greedy stays ahead):** among all valid choices for the first `i` products, the smallest possible `last` is best. Anything the next product could do after a larger `last`, it can also do after the smaller one.
- **Always feasible:** the greedy `last` never exceeds the current product's release time, because release times are sorted. So the release time itself is always allowed.
- **Why ties sort by alternate:** with equal release times the order is free. Taking smaller alternates first lets more of them be used.

## Solution

```java
import java.util.*;

class Solution {
    public int minimumReleaseTime(int[] release, int[] alternate) {
        int n = release.length;
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, Comparator.<Integer>comparingInt(i -> release[i]).thenComparingInt(i -> alternate[i]));

        int last = Integer.MIN_VALUE;
        for (int i : order) {
            // release[i] >= last always holds here, so there is always a legal choice
            last = alternate[i] >= last ? Math.min(alternate[i], release[i]) : release[i];
        }
        return n == 0 ? 0 : last;
    }
}
```

## Complexity

- **Time:** O(n log n) for the sort; the walk is O(n).
- **Space:** O(n) for the index array.

## Edge cases

- No products: 0 (a choice; state it). One product: the smaller of its two times.
- Alternate later than release: `Math.min` ignores it.
- Equal release times, for example `(5, 3)` and `(5, 2)`: sorting by alternate gives 2 then 3, so 3. The other order would give 5.
- An alternate too early for `last`: fall back to the release time.

## Variations

- **Order means array position.** Skip the sort. Now both times may be below `last`, so there can be no valid answer.

```java
class ByPosition {
    /** Returns -1 when no valid assignment exists. Times are non-negative. */
    int minimumReleaseTime(int[] release, int[] alternate) {
        int last = 0;
        for (int i = 0; i < release.length; i++) {
            int lo = Math.min(release[i], alternate[i]), hi = Math.max(release[i], alternate[i]);
            if (lo >= last) last = lo;
            else if (hi >= last) last = hi;
            else return -1;
        }
        return last;
    }
}
```

- **Only one release per time unit:** times must strictly increase, so a product can end up with no legal time. Products with equal release times then need a closer look.
- **Known relative:** Codeforces 479C "Exams", where the alternate is always earlier.

Related: [T6 · Dynamic programming and greedy](../academy/lessons/T6.md).
