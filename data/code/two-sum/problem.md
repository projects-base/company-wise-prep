Given an array of integers `nums` and an integer `target`, return the indices of the two different elements whose values add up to `target`. Exactly one such pair exists, and you may not use the same element twice. The pair may be returned in either order.

**Example 1**
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Why: nums[0] + nums[1] = 2 + 7 = 9.

**Example 2**
Input: nums = [3,2,4], target = 6
Output: [1,2]

**Example 3**
Input: nums = [3,3], target = 6
Output: [0,1]

**Constraints**
- 2 ≤ nums.length ≤ 10⁵
- −10⁹ ≤ nums[i], target ≤ 10⁹
- exactly one valid answer exists

**Notes**: aim for better than O(n²) — the hidden tests include 100,000 numbers.
