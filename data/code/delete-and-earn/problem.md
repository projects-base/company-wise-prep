You are given an array `nums`. You may repeat this move as often as you like: pick any remaining element `x`, earn `x` points, and delete that element — but doing so also deletes **every** remaining element equal to `x - 1` or `x + 1` (without earning anything for them). Return the most points you can collect.

**Example 1**
Input: nums = [3,4,2]
Output: 6
Why: take 4 (deleting the 3), then take 2.

**Example 2**
Input: nums = [2,2,3,3,3,4]
Output: 9
Why: take a 3 (deleting both 2s and the 4), then the other two 3s, for 3 + 3 + 3.

**Constraints**
- 1 ≤ nums.length ≤ 2 · 10⁴
- 1 ≤ nums[i] ≤ 10⁴

**Notes**: taking one copy of `x` never stops you from taking the other copies of `x`. The hidden tests include 20,000 numbers; plain recursion over choices will not finish.
