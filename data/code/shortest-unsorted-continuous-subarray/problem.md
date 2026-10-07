Given an integer array `nums`, find the shortest contiguous subarray such that sorting just that subarray in non-decreasing order leaves the whole array sorted in non-decreasing order. Return its length, or `0` if the array is already sorted.

**Example 1**
Input: nums = [2,6,4,8,10,9,15]
Output: 5
Why: sorting [6,4,8,10,9] makes the whole array sorted.

**Example 2**
Input: nums = [1,2,3,4]
Output: 0

**Example 3**
Input: nums = [1]
Output: 0

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁵ ≤ nums[i] ≤ 10⁵

**Notes**: an O(n) solution exists; O(n log n) by sorting a copy is also fine.
