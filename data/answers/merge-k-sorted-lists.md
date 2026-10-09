**Short answer:** Put the head of every non-empty list in a min-heap ordered by value. Repeatedly poll the smallest node, append it to the result, and push its `next` if there is one. The heap never holds more than k nodes, so each of the N nodes costs O(log k): O(N log k) total. Divide and conquer (merge lists in pairs, round after round) has the same bound.

## Approach

- **Brute force:** collect all values, sort, rebuild a list. O(N log N) time and O(N) extra space.
- **Merge one by one:** merge list 1 with list 2, then the result with list 3, and so on. Early nodes are copied again in every round: O(k · N).
- **Key insight:** at any moment the next output node is the smallest among the k current heads. A min-heap gives that minimum in O(log k).
- **Divide and conquer:** merge pairs (1,2), (3,4), … then pairs of the results. log k rounds, each touching all N nodes: O(N log k), and only O(1) extra space if done iteratively.

## Solution

`ListNode` is the usual LeetCode class.

```java
import java.util.PriorityQueue;

class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> heap = new PriorityQueue<>((a, b) -> Integer.compare(a.val, b.val));
        for (ListNode head : lists) if (head != null) heap.add(head);
        ListNode dummy = new ListNode(0), tail = dummy;
        while (!heap.isEmpty()) {
            ListNode n = heap.poll();
            tail = tail.next = n;               // reuse the node, no copying
            if (n.next != null) heap.add(n.next);
        }
        tail.next = null;
        return dummy.next;
    }
}
```

## Complexity

- **Time:** O(N log k): N polls and up to N pushes on a heap of size ≤ k.
- **Space:** O(k) for the heap. The result reuses the existing nodes.

## Edge cases

- `lists` is empty, or contains only empty lists: return `null` (the dummy's `next`).
- Null entries in the array: skip them when filling the heap.
- k = 1: the list itself.
- Duplicate values across lists: the heap handles ties in any order; stability is not required.
- Comparator: `Integer.compare(a.val, b.val)`, not `a.val - b.val`, to avoid overflow with extreme values.

## Variations

- **Divide-and-conquer version:**

```java
class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists.length == 0) return null;
        for (int step = 1; step < lists.length; step *= 2) {
            for (int i = 0; i + step < lists.length; i += 2 * step) {
                lists[i] = mergeTwo(lists[i], lists[i + step]);
            }
        }
        return lists[0];
    }

    private ListNode mergeTwo(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), t = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { t.next = a; a = a.next; } else { t.next = b; b = b.next; }
            t = t.next;
        }
        t.next = a != null ? a : b;
        return dummy.next;
    }
}
```

- **K sorted arrays or files too big for memory:** the same heap over iterators or file readers (external merge sort).
- **Kth smallest across k sorted lists:** stop after k polls.
- **Smallest range covering elements from k lists:** heap plus tracking the current maximum.

Practise it in the app: Run / Submit on this page.
