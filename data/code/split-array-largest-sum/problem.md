Given an array `nums` of non-negative integers and an integer `k`, cut `nums` into exactly `k` non-empty contiguous pieces. Each piece has a sum; among those `k` sums, look at the largest. Return the smallest possible value of that largest sum over all ways of cutting.

**Example 1**
Input: nums = [7,2,5,10,8], k = 2
Output: 18
Why: [7,2,5] and [10,8] have sums 14 and 18; no cut does better.

**Example 2**
Input: nums = [1,2,3,4,5], k = 2
Output: 9
Why: [1,2,3] | [4,5].

**Example 3**
Input: nums = [1,4,4], k = 3
Output: 4

**Constraints**
- 1 ≤ nums.length ≤ 1000
- 0 ≤ nums[i] ≤ 10⁶
- 1 ≤ k ≤ min(50, nums.length)

**Notes**: a DP over (prefix, pieces) works; binary search on the answer with a greedy check is faster. The total of all numbers is at most 10⁹, so it fits in an `int`.
