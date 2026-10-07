import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] available = in.nextIntArray();
        int tasks = in.nextInt();
        int duration = in.nextInt();
        IO.print(new Solution().earliestCompletion(available, tasks, duration));
    }
}
