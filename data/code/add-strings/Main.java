import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String num1 = in.nextString();
        String num2 = in.nextString();
        IO.print(new Solution().addStrings(num1, num2));
    }
}
