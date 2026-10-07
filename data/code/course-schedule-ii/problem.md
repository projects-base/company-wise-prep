There are `numCourses` courses labelled `0` to `numCourses - 1`. Each pair `prerequisites[i] = [a, b]` means course `b` must be completed before course `a`. Return an order in which all the courses can be taken. If several orders work, return any of them. If no order exists (the prerequisites contain a cycle), return an empty array.

**Example 1**
Input: numCourses = 2, prerequisites = [[1,0]]
Output: [0,1] — the judge prints "valid order" for any correct order
Why: course 0 has to come before course 1.

**Example 2**
Input: numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]
Output: [0,1,2,3] (or [0,2,1,3])

**Example 3**
Input: numCourses = 2, prerequisites = [[1,0],[0,1]]
Output: []
Why: each course requires the other, so neither can be taken first.

**Constraints**
- 1 ≤ numCourses ≤ 2000
- 0 ≤ prerequisites.length ≤ 10⁴
- 0 ≤ a, b < numCourses, a ≠ b, and all pairs are distinct

**Notes**: any valid order is accepted. The judge checks your order and prints `"valid order"` when it respects every prerequisite and lists each course exactly once; an empty result prints `[]`. Anything else prints `"invalid order: ..."` with what you returned.
