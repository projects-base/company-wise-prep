You are given an array of `k` singly linked lists, each already sorted in non-decreasing order. Merge them all into one sorted linked list and return its head. The input is written as a list of lists, e.g. `[[1,4,5],[1,3,4]]`; the checker builds the `ListNode` chains for you and prints the list you return.

**Example 1**
Input: lists = [[1,4,5],[1,3,4],[2,6]]
Output: [1,1,2,3,4,4,5,6]

**Example 2**
Input: lists = []
Output: []

**Example 3**
Input: lists = [[]]
Output: []

**Constraints**
- 0 ≤ k ≤ 10⁴
- 0 ≤ length of each list ≤ 500, total nodes ≤ 2 · 10⁴
- −10⁴ ≤ node value ≤ 10⁴

**Notes**: return `null` for an empty result. Merging lists one by one into a growing result is O(k · N); a min-heap of list heads or divide-and-conquer pairing gives O(N log k). The hidden tests include 2,000 lists with about 14,000 nodes.
