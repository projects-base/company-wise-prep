Given an integer array `nums` and an integer `k`, return `true` if there are two different indices `i` and `j` with `nums[i] == nums[j]` and `|i − j| ≤ k`; otherwise return `false`.

**Example 1**
Input: nums = [1,2,3,1], k = 3
Output: true
Why: nums[0] == nums[3] and the indices are 3 apart.

**Example 2**
Input: nums = [1,0,1,1], k = 1
Output: true
Why: nums[2] == nums[3].

**Example 3**
Input: nums = [1,2,3,1,2,3], k = 2
Output: false
Why: every equal pair is 3 indices apart.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁹ ≤ nums[i] ≤ 10⁹
- 0 ≤ k ≤ 10⁵

**Notes**: the interview variant asked you to *print* the duplicate values found within distance k — the same sliding window (a set of the last k values, or a map of each value's last index) finds them; here you only report whether one exists. The hidden tests include 10,000 numbers with a large k, where an O(n·k) scan does ~5 · 10⁷ comparisons; one pass with a hash map or set is O(n).
