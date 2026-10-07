import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<List<Integer>> raw = in.nextIntListList();
        ListNode[] lists = new ListNode[raw.size()];
        for (int i = 0; i < lists.length; i++) {
            ListNode dummy = new ListNode(0), tail = dummy;
            for (int v : raw.get(i)) tail = tail.next = new ListNode(v);
            lists[i] = dummy.next;
        }
        ListNode merged = new Solution().mergeKLists(lists);
        // IO prints a ListNode as [a,b,...]; an empty result prints [].
        IO.print(merged == null ? new ArrayList<Integer>() : merged);
    }
}
