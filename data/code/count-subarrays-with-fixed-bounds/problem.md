You are given an integer array `nums` and two integers `minK` and `maxK`. A *fixed-bound subarray* is a non-empty contiguous subarray whose minimum value is exactly `minK` and whose maximum value is exactly `maxK`. Return how many fixed-bound subarrays `nums` has.

**Example 1**
Input: nums = [1,3,5,2,7,5], minK = 1, maxK = 5
Output: 2
Why: [1,3,5] and [1,3,5,2]; any subarray containing 7 has the wrong maximum.

**Example 2**
Input: nums = [1,1,1,1], minK = 1, maxK = 1
Output: 10
Why: all 10 subarrays have minimum 1 and maximum 1.

**Constraints**
- 2 ≤ nums.length ≤ 10⁵
- 1 ≤ nums[i], minK, maxK ≤ 10⁶

**Notes**: the count can exceed the range of `int`, so the method returns `long`. If `minK > maxK` the answer is 0. The hidden tests include 70,000 numbers.
