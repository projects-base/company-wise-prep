import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] values = in.nextIntArray();
        int pos = in.nextInt();
        ListNode[] nodes = new ListNode[values.length];
        for (int i = 0; i < values.length; i++) nodes[i] = new ListNode(values[i]);
        for (int i = 0; i + 1 < values.length; i++) nodes[i].next = nodes[i + 1];
        if (pos >= 0) nodes[values.length - 1].next = nodes[pos];
        // Remember the original structure so we can tell if the solution changed it.
        ListNode[] nextBefore = new ListNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) nextBefore[i] = nodes[i].next;

        ListNode ans = new Solution().detectCycle(nodes.length == 0 ? null : nodes[0]);

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].next != nextBefore[i] || nodes[i].val != values[i]) {
                IO.print("the list was modified");
                return;
            }
        }
        if (ans == null) {
            IO.print(-1);
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] == ans) {
                IO.print(i);
                return;
            }
        }
        IO.print("returned a node that is not in the list");
    }
}
