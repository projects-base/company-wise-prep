Given an integer array `nums` (which may contain negative numbers and zeros) and an integer `k`, count how many contiguous, non-empty subarrays have a sum of exactly `k`. Subarrays at different positions are counted separately even if they hold the same values.

**Example 1**
Input: nums = [1,1,1], k = 2
Output: 2
Why: [1,1] starting at index 0 and [1,1] starting at index 1.

**Example 2**
Input: nums = [1,2,3], k = 3
Output: 2
Why: [1,2] and [3].

**Constraints**
- 1 ≤ nums.length ≤ 5 · 10⁴
- −1000 ≤ nums[i] ≤ 1000
- −10⁷ ≤ k ≤ 10⁷
- the answer fits in a 32-bit int

**Notes**: the hidden tests include 40,000 numbers, so trying every subarray (O(n²)) will be slow. Because values can be negative, a sliding window does not work; think prefix sums.
