Given an integer array `nums` and an integer `k`, return the `k` values that occur most often in `nums`. The tests guarantee the answer is unique: the k-th and (k+1)-th highest frequencies are never tied.

**Example 1**
Input: nums = [1,1,1,2,2,3], k = 2
Output: [1,2]
Why: 1 occurs three times and 2 twice; 3 only once.

**Example 2**
Input: nums = [1], k = 1
Output: [1]

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁴ ≤ nums[i] ≤ 10⁴
- 1 ≤ k ≤ number of distinct values

**Notes**: any order is accepted; the checker sorts your answer before comparing. Aim for better than O(n log n) — for example a bucket by frequency, or a heap of size k. The hidden tests include about 25,000 numbers.
