You are given two arrays `nums1` and `nums2`, each sorted in non-decreasing order (either may be empty, but not both). Return the median of all the numbers in the two arrays taken together. If the combined count is even, the median is the average of the two middle values.

Try to do it in O(log(m + n)) time, without merging the arrays.

**Example 1**
Input: nums1 = [1,3], nums2 = [2]
Output: 2.00000
Why: the combined sorted list is [1,2,3]; the middle value is 2.

**Example 2**
Input: nums1 = [1,2], nums2 = [3,4]
Output: 2.50000
Why: the combined list is [1,2,3,4]; the median is (2 + 3) / 2.

**Constraints**
- 0 ≤ m, n ≤ 10⁴ and 1 ≤ m + n
- −10⁶ ≤ nums1[i], nums2[i] ≤ 10⁶
- both arrays are sorted in non-decreasing order

**Notes**: the result is printed with 5 decimal places.
