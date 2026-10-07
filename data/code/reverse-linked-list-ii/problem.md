You are given the head of a singly linked list and two positions `left ≤ right` (1-based). Reverse only the nodes from position `left` to position `right`, leave the rest of the list as it is, and return the head of the resulting list. Try to do it in one pass.

**Example 1**
Input: head = [1,2,3,4,5], left = 2, right = 4
Output: [1,4,3,2,5]

**Example 2**
Input: head = [5], left = 1, right = 1
Output: [5]

**Example 3**
Input: head = [3,5], left = 1, right = 2
Output: [5,3]

**Constraints**
- 1 ≤ number of nodes ≤ 10⁵
- −500 ≤ Node.val ≤ 500
- 1 ≤ left ≤ right ≤ number of nodes
