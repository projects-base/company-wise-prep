import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        TreeNode root = in.nextTree();
        int start = in.nextInt();
        IO.print(new Solution().amountOfTime(root, start));
    }
}
