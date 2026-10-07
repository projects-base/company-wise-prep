Given the `head` of a singly linked list, return the node where a **cycle begins** — the first node you reach again by following `next` pointers — or `null` if the list has no cycle.

In the tests, the list is written as its values plus an integer `pos`: the last node's `next` points back to the node at index `pos` (0-based), or `pos = -1` means there is no cycle. `pos` is **not** passed to your method. The checker prints the **index** of the node you return (`-1` for `null`).

You must not modify the list.

**Example 1**
Input: head = [3,2,0,-4], pos = 1
Output: 1
Why: the tail links back to the node with value 2, at index 1.

**Example 2**
Input: head = [1,2], pos = 0
Output: 0

**Example 3**
Input: head = [1], pos = -1
Output: -1
Why: there is no cycle.

**Constraints**
- 0 ≤ number of nodes ≤ 10⁴
- −10⁵ ≤ node value ≤ 10⁵ (values may repeat, so compare nodes, not values)
- pos is −1 or a valid index

**Notes**: a hash set of visited nodes works in O(n) extra space. Floyd's tortoise-and-hare finds the entry in O(1) space: after the two pointers meet, a pointer from the head and a pointer from the meeting point, both moving one step at a time, meet exactly at the cycle's entry.
