import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        TreeNode root = in.nextTree();
        IO.print(new Solution().zigzagLevelOrder(root));
    }
}
