import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<List<String>> equations = in.nextStringListList();
        double[] values = in.nextDoubleArray();
        List<List<String>> queries = in.nextStringListList();
        IO.print(new Solution().calcEquation(equations, values, queries));
    }
}
