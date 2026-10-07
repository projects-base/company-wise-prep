Given an integer array `nums`, return an array `counts` of the same length where `counts[i]` is how many elements to the **right** of position `i` are **strictly smaller** than `nums[i]`.

**Example 1**
Input: nums = [5,2,6,1]
Output: [2,1,1,0]
Why: to the right of 5 are 2 and 1; to the right of 2 is 1; to the right of 6 is 1; nothing is right of the last 1.

**Example 2**
Input: nums = [-1]
Output: [0]

**Example 3**
Input: nums = [-1,-1]
Output: [0,0]
Why: equal values do not count — only strictly smaller ones.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁴ ≤ nums[i] ≤ 10⁴

**Notes**: the naive double loop is O(n²); the hidden tests include 12,000 numbers. A Fenwick tree over values, or a merge sort that counts, gives O(n log n).
