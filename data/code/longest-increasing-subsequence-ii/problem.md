You are given an integer array `nums` and an integer `k`. Find the length of the longest subsequence of `nums` that is **strictly increasing** and in which every two neighbouring elements of the subsequence differ by **at most `k`**. A subsequence keeps the original order but may skip elements.

**Example 1**
Input: nums = [4,2,1,4,3,4,5,8,15], k = 3
Output: 5
Why: [1,3,4,5,8] is strictly increasing and each step is at most 3. No valid subsequence of length 6 exists.

**Example 2**
Input: nums = [7,4,5,1,8,12,4,7], k = 5
Output: 4
Why: [4,5,8,12].

**Example 3**
Input: nums = [1,5], k = 1
Output: 1
Why: 5 − 1 = 4 > 1, so only single elements qualify.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- 1 ≤ nums[i], k ≤ 10⁵

**Notes**: with k = 1 this is "longest run of consecutive values taken in order". The classic O(n²) LIS DP is too slow for the hidden tests; think about a structure indexed by value that answers "best length ending at a value in [v − k, v − 1]" quickly.
