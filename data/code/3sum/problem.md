Given an integer array `nums`, find every triplet of values `[a, b, c]` taken from three **different positions** of the array such that `a + b + c = 0`. Return each distinct triplet of values once — two triplets count as the same if they contain the same three values, regardless of which positions they came from or the order they are listed in.

**Example 1**
Input: nums = [-1,0,1,2,-1,-4]
Output: [[-1,-1,2],[-1,0,1]]
Why: -1 + -1 + 2 = 0 and -1 + 0 + 1 = 0. The value triplet [-1,0,1] can be formed in two ways but is listed once.

**Example 2**
Input: nums = [0,1,1]
Output: []

**Example 3**
Input: nums = [0,0,0]
Output: [[0,0,0]]

**Constraints**
- 3 ≤ nums.length ≤ 3000
- −10⁵ ≤ nums[i] ≤ 10⁵

**Notes**: any order is accepted, both for the triplets in the list and for the values inside each triplet — the checker sorts each triplet and then the list before comparing. The hidden tests include a few thousand numbers, so the O(n³) triple loop is too slow; aim for O(n²).
