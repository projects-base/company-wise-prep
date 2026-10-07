Given the head of a singly linked list and a positive integer `k`, reverse the nodes of the list in consecutive blocks of `k`: the first `k` nodes are reversed, then the next `k`, and so on. If fewer than `k` nodes remain at the end, leave that tail in its original order. Return the head of the resulting list.

Rearrange the nodes themselves — do not just rewrite the values.

**Example 1**
Input: head = [1,2,3,4,5], k = 2
Output: [2,1,4,3,5]

**Example 2**
Input: head = [1,2,3,4,5], k = 3
Output: [3,2,1,4,5]
Why: only one full block of three; the remaining [4,5] stays as it is.

**Constraints**
- 1 ≤ number of nodes ≤ 5000
- 0 ≤ node value ≤ 1000
- 1 ≤ k ≤ number of nodes

**Notes**: try to use only O(1) extra memory besides the list itself.
