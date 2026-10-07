import java.util.*;

class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(0, head);
        ListNode groupPrev = dummy;
        while (true) {
            // find the k-th node of this block
            ListNode kth = groupPrev;
            for (int i = 0; i < k && kth != null; i++) kth = kth.next;
            if (kth == null) break;
            ListNode groupNext = kth.next;
            // reverse groupPrev.next .. kth
            ListNode prev = groupNext, cur = groupPrev.next;
            while (cur != groupNext) {
                ListNode nx = cur.next;
                cur.next = prev;
                prev = cur;
                cur = nx;
            }
            ListNode first = groupPrev.next;
            groupPrev.next = kth;
            groupPrev = first;
        }
        return dummy.next;
    }
}
