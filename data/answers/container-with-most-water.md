**Short answer:** Use two pointers starting at both ends. Compute the area, then move the pointer at the shorter line inward. Moving the taller line can never help, because the width shrinks and the height is still capped by the shorter line. One pass, O(n) time, O(1) space.

## Approach

**Brute force.** Try every pair `i < j` and take the maximum of `min(h[i], h[j]) * (j − i)`. O(n²), too slow for 10⁵ lines.

**Key insight.** Start with the widest container, `l = 0` and `r = n − 1`. Suppose `h[l] < h[r]`. Any container that keeps `l` and uses some `r' < r` is narrower, and its height is at most `h[l]`. So none of them can beat the area we just computed. We can safely discard `l` and move it right. The same argument applies to `r` when it is the shorter side. Each step discards one line without missing the optimum.

When the heights are equal, moving either side is safe: any better container must exclude both current lines, since it would be narrower and still capped by that height.

## Solution

```java
import java.util.*;

class Solution {
    public int maxArea(int[] height) {
        int l = 0, r = height.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, Math.min(height[l], height[r]) * (r - l));
            // moving the taller side can never help, so move the shorter one
            if (height[l] < height[r]) l++;
            else r--;
        }
        return best;
    }
}
```

## Complexity

- **Time:** O(n). Each step moves one pointer, so there are at most n − 1 steps.
- **Space:** O(1).

## Edge cases

- Two lines: one container, `min(h0, h1) * 1`.
- Zero heights: area 0, still handled.
- Overflow: at most 10⁴ × 10⁵ = 10⁹, which fits in `int` (max about 2.1·10⁹).
- All lines equal: the answer is the outermost pair.

## Variations

- **Trapping Rain Water** looks similar but sums water over every bar; it also uses two pointers, moving the side with the smaller running maximum.
- A small speed-up: after moving a pointer, skip lines no taller than the one you left, since they cannot produce a larger area. It does not change the O(n) bound.

See [C4 · The optimisation playbook](../academy/lessons/C4.md) for explaining the "why can we discard this" step out loud.

Practise it in the app: Run / Submit on this page.
