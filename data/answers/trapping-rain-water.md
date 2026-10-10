**Short answer:** Water above bar `i` is `min(maxLeft, maxRight) - height[i]`. Two pointers make this O(n) time and O(1) space. Move inwards from the side with the lower bar. That side's running max is already the binding limit, because the other side has a bar at least as tall. Add `runningMax - height` at each step.

## Picture it

`height = [2,0,3,1,0,2]`, answer 5. The side with the lower (or equal) bar moves.

```text
index   0 1 2 3 4 5
height  2 0 3 1 0 2
start   l         r
```

| Step | l | r | h[l] vs h[r] | Side moved | leftMax | rightMax | Water added | Total |
|---|---|---|---|---|---|---|---|---|
| 1 | 0 | 5 | 2 vs 2 (not <) | right, settle 5 | 0 | 2 | 2 − 2 = 0 | 0 |
| 2 | 0 | 4 | 2 vs 0 | right, settle 4 | 0 | 2 | 2 − 0 = 2 | 2 |
| 3 | 0 | 3 | 2 vs 1 | right, settle 3 | 0 | 2 | 2 − 1 = 1 | 3 |
| 4 | 0 | 2 | 2 vs 3 | left, settle 0 | 2 | 2 | 2 − 2 = 0 | 3 |
| 5 | 1 | 2 | 0 vs 3 | left, settle 1 | 2 | 2 | 2 − 0 = 2 | 5 |
| – | 2 | 2 | l == r, stop | | | | | **5** |

**The picture in one sentence:** the lower side's running max is already the binding wall (the other side is at least as tall), so settle that bar and move inwards.

## Approach

- **Brute force.** For each position, scan left and right for the tallest bars. O(n²).
- **Prefix maxima.** Precompute `leftMax[i]` and `rightMax[i]` in two passes, then sum `min(leftMax, rightMax) - height`. O(n) time and O(n) space. Say this one first: it is easy to prove correct.
- **Key insight (two pointers).** Suppose `height[l] < height[r]`. Every bar right of `l` is bounded by something at least `height[r]`, so the true right max for `l` is at least `height[r] > height[l]`. Water at `l` therefore depends only on `leftMax`, and you can settle `l` and move on. The right side works the same way.
- **Monotonic stack** (third option): pop bars that are lower than the current bar and add the water trapped in horizontal layers. Also O(n). It is useful when the question asks for water per basin.

## Solution

```java
class Solution {
    public int trap(int[] height) {
        int l = 0, r = height.length - 1, leftMax = 0, rightMax = 0, water = 0;
        while (l < r) {
            // the lower side is bounded by its own running max
            if (height[l] < height[r]) {
                leftMax = Math.max(leftMax, height[l]);
                water += leftMax - height[l++];
            } else {
                rightMax = Math.max(rightMax, height[r]);
                water += rightMax - height[r--];
            }
        }
        return water;
    }
}
```

## Complexity

- **Time:** O(n). Each step moves one pointer.
- **Space:** O(1).

## Edge cases

- Fewer than three bars: no water.
- Strictly increasing or decreasing heights: 0.
- Flat plateaus and equal walls such as `[3,0,3]` give 3. The `else` branch handles ties.
- Overflow: the problem says the answer fits in an `int`. In general, use `long` (10⁵ bars × 10⁵ height).

## Variations

- **Trapping Rain Water II (2D):** a min-heap that grows inwards from the border.
- **Container With Most Water:** same two pointers, but with only two walls and no bars in between.
- **Streaming bars:** the monotonic stack handles bars arriving left to right.

Practise it in the app: Run / Submit on this page.
