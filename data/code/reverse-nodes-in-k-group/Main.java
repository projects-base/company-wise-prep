import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        ListNode head = in.nextLinkedList();
        int k = in.nextInt();
        IO.print(new Solution().reverseKGroup(head, k));
    }
}
