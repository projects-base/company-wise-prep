Given an integer array `nums`, find the contiguous, non-empty block of elements with the largest sum and return that sum.

**Example 1**
Input: nums = [-2,1,-3,4,-1,2,1,-5,4]
Output: 6
Why: the block [4,-1,2,1] sums to 6, and no other block does better.

**Example 2**
Input: nums = [5,4,-1,7,8]
Output: 23
Why: the whole array is the best block.

**Example 3**
Input: nums = [-3,-1,-2]
Output: -1
Why: the block must be non-empty, so the best is the single largest element.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁴ ≤ nums[i] ≤ 10⁴

**Notes**: the hidden tests include 30,000 numbers, so checking every block (O(n²)) will be slow. Aim for O(n).
