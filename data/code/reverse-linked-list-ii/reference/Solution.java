import java.util.*;

class Solution {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0, head);
        ListNode before = dummy;
        for (int i = 1; i < left; i++) before = before.next;
        ListNode tail = before.next; // ends up as the last node of the reversed block
        for (int i = left; i < right; i++) {
            ListNode moved = tail.next;
            tail.next = moved.next;
            moved.next = before.next;
            before.next = moved;
        }
        return dummy.next;
    }
}
