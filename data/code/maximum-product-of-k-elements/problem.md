Given an integer array `nums`, which may contain negatives and zeros, and an integer `k`, choose exactly `k` elements at different indices so that their product is as large as possible. Return that product.

**Example 1**
Input: nums = [1,10,-5,1,-10], k = 3
Output: 500
Why: 10 × (−5) × (−10) = 500. The two negatives cancel out.

**Example 2**
Input: nums = [-4,-3,-2,-1], k = 3
Output: -6
Why: every choice is negative, so pick the three values closest to zero: (−3) × (−2) × (−1) = −6.

**Example 3**
Input: nums = [0,-1,2,-3], k = 2
Output: 3
Why: (−1) × (−3) = 3 beats 2 × 0 = 0.

**Constraints**
- 1 ≤ k ≤ nums.length ≤ 10⁵
- k ≤ 18
- −10 ≤ nums[i] ≤ 10, so every product fits in a `long`

**Notes**: sort, then decide greedily from both ends. Comparing the product of the two smallest values with the product of the two largest tells you whether a pair of negatives beats a pair of positives. Be careful when k is odd and every value is ≤ 0. Follow-ups: use no extra space after sorting, then avoid the full sort with heaps, since only the k largest and k smallest values matter.
