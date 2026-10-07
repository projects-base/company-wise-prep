An elevation map is given as an array `height` of non-negative integers, where each entry is the height of a bar of width 1 placed side by side. After it rains, water collects in the dips between bars; water above a position can rise only as high as the shorter of the tallest bars to its left and to its right. Return the total units of water held.

**Example 1**
Input: height = [0,1,0,2,1,0,1,3,2,1,2,1]
Output: 6

**Example 2**
Input: height = [4,2,0,3,2,5]
Output: 9
Why: the positions hold 0, 2, 4, 1, 2 and 0 units.

**Constraints**
- 1 ≤ height.length ≤ 10⁵
- 0 ≤ height[i] ≤ 10⁵
- the answer fits in a 32-bit int

**Notes**: the hidden tests include 30,000 bars, so scanning left and right from every position (O(n²)) will be slow. Aim for O(n) time.
