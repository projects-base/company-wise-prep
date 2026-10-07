An array `nums` holds `n + 1` integers, each between `1` and `n` inclusive. Exactly one value appears more than once (it may appear two or more times); every other value appears at most once. Return the repeated value.

You must **not modify** `nums`, and you should use only O(1) extra memory.

**Example 1**
Input: nums = [1,3,4,2,2]
Output: 2

**Example 2**
Input: nums = [3,1,3,4,2]
Output: 3

**Example 3**
Input: nums = [3,3,3,3,3]
Output: 3
Why: the repeated value may appear many times.

**Constraints**
- 1 ≤ n ≤ 10⁵, nums.length = n + 1
- 1 ≤ nums[i] ≤ n
- exactly one value is repeated

**Notes**: the judge checks that your method leaves `nums` unchanged — sorting it or marking entries as negative will be reported as `"array was modified"`. Aim for O(n) time (Floyd's cycle detection) or O(n log n) (binary search on the value range). The hidden tests include 25,000 numbers.
