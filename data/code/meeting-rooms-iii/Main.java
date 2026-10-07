import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int n = in.nextInt();
        int[][] meetings = in.nextIntMatrix();
        IO.print(new Solution().mostBooked(n, meetings));
    }
}
