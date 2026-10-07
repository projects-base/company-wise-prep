import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        double x = in.nextDouble();
        int precision = in.nextInt();
        IO.print(new Solution().ftoa(x, precision));
    }
}
