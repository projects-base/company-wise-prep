**Short answer:** Use two pointers starting at both ends. Compute the area, then move the pointer at the shorter line inward. Moving the taller line can never help, because the width shrinks and the height is still capped by the shorter line. One pass, O(n) time, O(1) space.

## Picture it

`height = [1,8,6,2,5,4,8,3,7]` (indices 0–8). Start fully wide:

```text
index:  0 1 2 3 4 5 6 7 8
height: 1 8 6 2 5 4 8 3 7
        ^l              ^r
```

| Step | l (h) | r (h) | Area `min × width` | best | Move |
|---|---|---|---|---|---|
| 1 | 0 (1) | 8 (7) | 1 × 8 = 8 | 8 | left is shorter: `l++` |
| 2 | 1 (8) | 8 (7) | 7 × 7 = 49 | **49** | right is shorter: `r--` |
| 3 | 1 (8) | 7 (3) | 3 × 6 = 18 | 49 | `r--` |
| 4 | 1 (8) | 6 (8) | 8 × 5 = 40 | 49 | equal: `r--` |
| 5 | 1 (8) | 5 (4) | 4 × 4 = 16 | 49 | `r--` |
| 6 | 1 (8) | 4 (5) | 5 × 3 = 15 | 49 | `r--` |
| 7 | 1 (8) | 3 (2) | 2 × 2 = 4 | 49 | `r--` |
| 8 | 1 (8) | 2 (6) | 6 × 1 = 6 | 49 | `r--`, now `l == r`: stop |

Answer **49**.

**The picture in one sentence:** the shorter wall caps every narrower container that keeps it, so it can be thrown away after one area check.

## Watch it

Press **Play**, or step with **→**. The highlighted code line is the one that just ran.

```anim
{"title":"Container With Most Water: two pointers","code":["int maxArea(int[] h) {","  int l = 0, r = h.length - 1, best = 0;","  while (l < r) {","    int area = Math.min(h[l], h[r]) * (r - l);","    best = Math.max(best, area);","    if (h[l] < h[r]) l++;","    else r--;","  }","  return best;","}"],"frames":[
{"note":"Start with the widest container: one pointer at each end.","line":2,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":0,"r":8},"range":[0,8]},"vars":{"best":0}},
{"note":"Area = min(1, 7) × (8 − 0) = 8. New best 8.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":0,"r":8},"range":[0,8],"marks":{"0":"cur","8":"cur"}},"vars":{"l":0,"r":8,"area":8,"best":8}},
{"note":"h[l] = 1 is the shorter wall: it caps every narrower container, so move l inward.","line":6,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":8},"range":[1,8]},"vars":{"l":1,"r":8,"best":8}},
{"note":"Area = min(8, 7) × (8 − 1) = 49. New best 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":8},"range":[1,8],"marks":{"1":"cur","8":"cur"}},"vars":{"l":1,"r":8,"area":49,"best":49}},
{"note":"h[r] = 7 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":7},"range":[1,7]},"vars":{"l":1,"r":7,"best":49}},
{"note":"Area = min(8, 3) × (7 − 1) = 18. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":7},"range":[1,7],"marks":{"1":"cur","7":"cur"}},"vars":{"l":1,"r":7,"area":18,"best":49}},
{"note":"h[r] = 3 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":6},"range":[1,6]},"vars":{"l":1,"r":6,"best":49}},
{"note":"Area = min(8, 8) × (6 − 1) = 40. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":6},"range":[1,6],"marks":{"1":"cur","6":"cur"}},"vars":{"l":1,"r":6,"area":40,"best":49}},
{"note":"h[r] = 8 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":5},"range":[1,5]},"vars":{"l":1,"r":5,"best":49}},
{"note":"Area = min(8, 4) × (5 − 1) = 16. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":5},"range":[1,5],"marks":{"1":"cur","5":"cur"}},"vars":{"l":1,"r":5,"area":16,"best":49}},
{"note":"h[r] = 4 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":4},"range":[1,4]},"vars":{"l":1,"r":4,"best":49}},
{"note":"Area = min(8, 5) × (4 − 1) = 15. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":4},"range":[1,4],"marks":{"1":"cur","4":"cur"}},"vars":{"l":1,"r":4,"area":15,"best":49}},
{"note":"h[r] = 5 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":3},"range":[1,3]},"vars":{"l":1,"r":3,"best":49}},
{"note":"Area = min(8, 2) × (3 − 1) = 4. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":3},"range":[1,3],"marks":{"1":"cur","3":"cur"}},"vars":{"l":1,"r":3,"area":4,"best":49}},
{"note":"h[r] = 2 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":2},"range":[1,2]},"vars":{"l":1,"r":2,"best":49}},
{"note":"Area = min(8, 6) × (2 − 1) = 6. Best stays 49.","line":5,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":2},"range":[1,2],"marks":{"1":"cur","2":"cur"}},"vars":{"l":1,"r":2,"area":6,"best":49}},
{"note":"h[r] = 6 is the shorter (or equal) wall: move r inward.","line":7,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1,"r":1},"range":[1,1]},"vars":{"l":1,"r":1,"best":49}},
{"note":"The pointers met. Best area = 49, in O(n): each step discards one wall for good.","line":9,"array":{"label":"height","values":[1,8,6,2,5,4,8,3,7],"pointers":{"l":1}},"vars":{"best":49}}
]}
```

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
