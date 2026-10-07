You are given an array `height` where `height[i]` is the height of a vertical line standing at position `i`. Choose two of the lines; together with the x-axis they form a container whose water level can reach the shorter line's height and whose width is the distance between them. Return the largest amount of water such a container can hold, i.e. the maximum of `min(height[i], height[j]) * (j - i)` over all `i < j`.

**Example 1**
Input: height = [1,8,6,2,5,4,8,3,7]
Output: 49
Why: lines at positions 1 (height 8) and 8 (height 7) give min(8, 7) × 7 = 49.

**Example 2**
Input: height = [1,1]
Output: 1

**Constraints**
- 2 ≤ height.length ≤ 10⁵
- 0 ≤ height[i] ≤ 10⁴

**Notes**: the hidden tests include 30,000 lines, so trying every pair (O(n²)) will be slow. Aim for O(n).
