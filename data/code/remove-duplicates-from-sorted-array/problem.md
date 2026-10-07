You are given an integer array `nums` sorted in non-decreasing order. Remove the repeated values **in place** so that each distinct value appears once, keeping their original relative order, and return the number `k` of distinct values. After your method returns, the first `k` slots of `nums` must hold the distinct values in order; whatever is left in the remaining slots does not matter.

The checker prints two lines: your `k`, then the first `k` elements of `nums`.

**Example 1**
Input: nums = [1,1,2]
Output:
2
[1,2]

**Example 2**
Input: nums = [0,0,1,1,1,2,2,3,3,4]
Output:
5
[0,1,2,3,4]

**Constraints**
- 1 ≤ nums.length ≤ 3 · 10⁴
- −100 ≤ nums[i] ≤ 100
- `nums` is sorted in non-decreasing order

**Notes**: use O(1) extra space — do not build a second array.
