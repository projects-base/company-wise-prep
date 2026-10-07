An array `nums` holds `n` items, each coloured red, white or blue, written as `0`, `1` and `2`. Rearrange the array **in place** so that all 0s come first, then all 1s, then all 2s. Do not use a library sort. The method returns nothing; the checker prints `nums` after your method runs.

**Example 1**
Input: nums = [2,0,2,1,1,0]
Output: [0,0,1,1,2,2]

**Example 2**
Input: nums = [2,0,1]
Output: [0,1,2]

**Constraints**
- 1 ≤ n ≤ 10⁵
- nums[i] is 0, 1 or 2

**Notes**: aim for a single pass with O(1) extra space (the Dutch national flag idea).
