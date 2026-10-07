import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] logs = in.nextStringArray();
        String query = in.nextString();
        IO.print(new Solution().searchLogs(logs, query));
    }
}
