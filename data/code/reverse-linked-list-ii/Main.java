import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        ListNode head = in.nextLinkedList();
        int left = in.nextInt();
        int right = in.nextInt();
        IO.print(new Solution().reverseBetween(head, left, right));
    }
}
