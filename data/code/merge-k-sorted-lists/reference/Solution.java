import java.util.*;

class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> heap = new PriorityQueue<>((a, b) -> Integer.compare(a.val, b.val));
        for (ListNode head : lists) if (head != null) heap.add(head);
        ListNode dummy = new ListNode(0), tail = dummy;
        while (!heap.isEmpty()) {
            ListNode n = heap.poll();
            tail = tail.next = n;
            if (n.next != null) heap.add(n.next);
        }
        tail.next = null;
        return dummy.next;
    }
}
