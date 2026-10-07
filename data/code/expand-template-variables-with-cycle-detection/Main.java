import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[][] pairs = in.nextStringMatrix();
        String template = in.nextString();
        Map<String, String> vars = new HashMap<>();
        for (String[] p : pairs) vars.put(p[0], p[1]);
        IO.print(new Solution().expand(vars, template));
    }
}
