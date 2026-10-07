You are given an integer array `nums` and a window size `k`. A window of `k` consecutive elements starts at the left end of the array and slides right one position at a time until it reaches the right end. Return an array holding the largest value inside the window at each of its `n - k + 1` positions, in order.

**Example 1**
Input: nums = [1,3,-1,-3,5,3,6,7], k = 3
Output: [3,3,5,5,6,7]
Why: the windows are [1,3,-1], [3,-1,-3], [-1,-3,5], [-3,5,3], [5,3,6] and [3,6,7].

**Example 2**
Input: nums = [1], k = 1
Output: [1]

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁴ ≤ nums[i] ≤ 10⁴
- 1 ≤ k ≤ nums.length

**Notes**: the hidden tests include 25,000 numbers with a window of 20,000, so scanning every window from scratch (O(n·k)) is too slow. Aim for O(n).
