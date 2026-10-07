import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[][] messages = in.nextStringMatrix();
        int k = in.nextInt();
        IO.print(new Solution().topKUsers(messages, k));
    }
}
