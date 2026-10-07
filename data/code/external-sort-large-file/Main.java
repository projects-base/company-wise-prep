import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] file = in.nextIntArray();
        int memory = in.nextInt();
        IO.print(new Solution().externalSort(file, memory));
    }
}
