Given an array of positive integers `nums`, decide whether the elements can be split into two groups (every element in exactly one group) with equal sums. Return `true` if such a split exists and `false` otherwise.

**Example 1**
Input: nums = [1,5,11,5]
Output: true
Why: {1, 5, 5} and {11} both sum to 11.

**Example 2**
Input: nums = [1,2,3,5]
Output: false
Why: the total is 11, which is odd, so no equal split exists.

**Constraints**
- 1 ≤ nums.length ≤ 200
- 1 ≤ nums[i] ≤ 100

**Notes**: trying every subset (2ⁿ of them) is far too slow. The hidden tests include 200 elements.
