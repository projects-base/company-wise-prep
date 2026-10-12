**Short answer:** Use two pointers from the head: `slow` moves one node per step, `fast` moves two. When `fast` reaches the end, `slow` has gone half the distance, so it is on the middle. This finds the middle in a single pass without knowing the length. For an even-length list, the loop condition `fast != null && fast.next != null` returns the **second** middle, which is what LeetCode expects.

## Approach

**Two passes.** Count the nodes (`n`), then walk `n / 2` steps from the head. Correct, O(n), but the question asks for a single iteration.

**Store in an array.** Copy nodes into a list and return `list.get(n / 2)`. One pass but O(n) extra space.

**Key insight (slow and fast pointers).** If one pointer moves at twice the speed of the other, then when the fast one has covered the whole list, the slow one has covered half. One pass, O(1) space.

**Trace on `1 → 2 → 3 → 4 → 5`:**

| Step | slow | fast | Continue? |
|---|---|---|---|
| start | 1 | 1 | yes |
| 1 | 2 | 3 | yes |
| 2 | 3 | 5 | no, `fast.next` is null |

Middle is 3. On `1 → 2 → 3 → 4 → 5 → 6`, `fast` ends at `null` after three steps and `slow` is on 4 (the second middle).

## Solution

```java
class Solution {
    // Second middle for even lengths (LeetCode 876).
    public ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    // First middle for even lengths: useful before splitting a list (merge sort, palindrome check).
    public ListNode firstMiddle(ListNode head) {
        if (head == null) return null;
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
```

## Complexity

- **Time:** O(n). `fast` visits about n nodes, `slow` about n/2, all in one loop.
- **Space:** O(1). Only two references.

## Edge cases

- Empty list: return `null` (the loop never runs).
- One node: returns that node.
- Two nodes: `middleNode` returns the second, `firstMiddle` returns the first. Ask which one the interviewer wants.
- Check `fast != null` before `fast.next`, or you get a `NullPointerException` on even lengths.

## Variations

- **Detect a cycle (Floyd):** same two pointers; if they ever meet, there is a cycle.
- **Delete the middle node:** stop `slow` one node earlier (or keep a `prev`), then `prev.next = slow.next`.
- **Palindrome linked list:** find the first middle, reverse the second half, compare the halves.
- **k-th node from the end:** move one pointer `k` ahead, then move both until the leader hits the end.
- **Split a list into halves (merge sort on a linked list):** use `firstMiddle`, then cut with `second = mid.next; mid.next = null`. Using the second middle here causes infinite recursion on two-node lists, because one half stays the whole list.
- **Why does it work in one pass?** After `t` steps, `slow` is at position `t` and `fast` at `2t`. The loop stops when `2t` reaches about `n`, so `t` is about `n / 2`.

See [H1 · Java idioms for coding interviews](../academy/lessons/H1.md).
