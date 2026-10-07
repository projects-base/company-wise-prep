A window of size `k` slides over the integer array `nums` from left to right, one position at a time. For every position of the window, return the median of the `k` numbers inside it. For an odd `k` the median is the middle value after sorting; for an even `k` it is the average of the two middle values.

**Example 1**
Input: nums = [1,3,-1,-3,5,3,6,7], k = 3
Output: [1.00000,-1.00000,-1.00000,3.00000,5.00000,6.00000]
Why: the windows are [1,3,-1], [3,-1,-3], [-1,-3,5], [-3,5,3], [5,3,6], [3,6,7].

**Example 2**
Input: nums = [1,2,3,4,2,3,1,4,2], k = 3
Output: [2.00000,3.00000,3.00000,3.00000,2.00000,3.00000,2.00000]

**Example 3**
Input: nums = [5,1,4,2], k = 2
Output: [3.00000,2.50000,3.00000]

**Constraints**
- 1 ≤ k ≤ nums.length ≤ 10⁵
- −2³¹ ≤ nums[i] ≤ 2³¹ − 1 (adding two values can overflow `int`)

**Notes**: the judge prints each median with 5 decimals. Sorting every window is too slow on the hidden tests; aim for O(n log k).
