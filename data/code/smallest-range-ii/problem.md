You are given an integer array `nums` and an integer `k`. For **every** index `i` you must change `nums[i]` exactly once, either to `nums[i] + k` or to `nums[i] - k` (each index chooses independently). The score of the resulting array is its maximum minus its minimum. Return the smallest score you can achieve.

**Example 1**
Input: nums = [1], k = 0
Output: 0

**Example 2**
Input: nums = [0,10], k = 2
Output: 6
Why: change to [2,8]; 8 − 2 = 6.

**Example 3**
Input: nums = [1,3,6], k = 3
Output: 3
Why: change to [4,6,3]; 6 − 3 = 3.

**Constraints**
- 1 ≤ nums.length ≤ 3 × 10⁴
- 0 ≤ nums[i] ≤ 10⁴
- 0 ≤ k ≤ 10⁴

**Notes**: there is no "leave it unchanged" option — every element moves by exactly k up or down. The hidden tests include 30,000 numbers, so an O(n²) scan is too slow; O(n log n) is expected.
