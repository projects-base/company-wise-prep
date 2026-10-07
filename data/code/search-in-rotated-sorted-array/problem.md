An array of **distinct** integers was sorted in ascending order and then possibly rotated at an unknown pivot: for some `p`, it became `[nums[p], …, nums[n-1], nums[0], …, nums[p-1]]` (with `p = 0` meaning not rotated at all). Given the rotated array `nums` and a value `target`, return the index of `target` in `nums`, or `-1` if it is not present.

Your algorithm should run in O(log n) time.

**Example 1**
Input: nums = [4,5,6,7,0,1,2], target = 0
Output: 4

**Example 2**
Input: nums = [4,5,6,7,0,1,2], target = 3
Output: -1

**Example 3**
Input: nums = [1], target = 0
Output: -1

**Constraints**
- 1 ≤ nums.length ≤ 5000
- −10⁴ ≤ nums[i], target ≤ 10⁴
- all values in nums are distinct
- nums is an ascending array rotated by some amount (possibly 0)

**Notes**: a linear scan gives the right answer but misses the point — interviewers expect a single modified binary search.
