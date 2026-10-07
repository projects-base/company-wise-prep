You are given a binary array `nums` (every element is 0 or 1) and an integer `k`. You may flip at most `k` of the zeros into ones. Return the length of the longest run of consecutive ones you can obtain.

**Example 1**
Input: nums = [1,1,1,0,0,0,1,1,1,1,0], k = 2
Output: 6
Why: flip nums[4] and nums[5] to get [1,1,1,0,1,1,1,1,1,1,0]; indices 4–9 are all ones.

**Example 2**
Input: nums = [0,0,1,1,0,0,1,1,1,0,1,1,0,0,0,1,1,1,1], k = 3
Output: 10
Why: flipping indices 4, 5 and 9 makes indices 2–11 all ones.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- nums[i] is 0 or 1
- 0 ≤ k ≤ nums.length

**Notes**: the hidden tests include an array of 60,000 elements.
