import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] products = in.nextStringArray();
        String searchWord = in.nextString();
        IO.print(new Solution().suggestedProducts(products, searchWord));
    }
}
