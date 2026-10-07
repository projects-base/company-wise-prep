You are given an unsorted array of integers `nums`. Find the length of the longest run of values that form a sequence of consecutive integers (x, x+1, x+2, …). The values may appear anywhere in the array and in any order, and duplicates count only once. Your algorithm should run in O(n) time.

**Example 1**
Input: nums = [100,4,200,1,3,2]
Output: 4
Why: the values 1, 2, 3 and 4 are all present.

**Example 2**
Input: nums = [0,3,7,2,5,8,4,6,0,1]
Output: 9
Why: every value from 0 to 8 is present.

**Example 3**
Input: nums = [1,0,1,2]
Output: 3

**Constraints**
- 0 ≤ nums.length ≤ 10⁵
- −10⁹ ≤ nums[i] ≤ 10⁹

**Notes**: an empty array has answer 0. The hidden tests include tens of thousands of values, so an O(n²) scan is too slow.
