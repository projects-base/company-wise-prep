import java.util.*;

class Solution {
    // Floyd: if slow and fast meet, the distance from head to the cycle entry equals the distance
    // from the meeting point to the entry (modulo the cycle length), so walk both one step at a time.
    public ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                ListNode a = head;
                while (a != slow) {
                    a = a.next;
                    slow = slow.next;
                }
                return a;
            }
        }
        return null;
    }
}
