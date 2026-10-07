Think of the array `nums` as the digits of a number (or any sequence of values). Rearrange it **in place** into the next arrangement that is larger in dictionary order — that is, the smallest rearrangement of the same values that is strictly greater than the current one. If no larger arrangement exists (the values are already in non-increasing order), rearrange them into the smallest arrangement, i.e. sorted ascending. Use only O(1) extra memory. The method returns nothing; the checker prints `nums` afterwards.

**Example 1**
Input: nums = [1,2,3]
Output: [1,3,2]

**Example 2**
Input: nums = [3,2,1]
Output: [1,2,3]
Why: 321 is the largest arrangement, so we wrap around to the smallest.

**Example 3**
Input: nums = [1,1,5]
Output: [1,5,1]
Why: the next bigger number made of the digits of 115 is 151.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- 0 ≤ nums[i] ≤ 100

**Notes**: the interview phrasing was "next bigger number with the same digits"; treat each array element as a digit. In that phrasing the number was left unchanged when no bigger one exists — here, as on LeetCode, wrap around to ascending order instead. The hidden tests include 25,000 elements.
