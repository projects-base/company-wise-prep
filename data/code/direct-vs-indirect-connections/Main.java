import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        PartnerNetwork net = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "PartnerNetwork" -> { net = new PartnerNetwork(matrix((List<?>) a.get(0))); out.add(null); }
                case "areConnected" -> out.add(net.areConnected(arg(a, 0), arg(a, 1)));
                case "areRelated" -> out.add(net.areRelated(arg(a, 0), arg(a, 1)));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    static int arg(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }

    static int[][] matrix(List<?> rows) {
        int[][] m = new int[rows.size()][];
        for (int i = 0; i < m.length; i++) {
            List<?> r = (List<?>) rows.get(i);
            m[i] = new int[r.size()];
            for (int j = 0; j < r.size(); j++) m[i][j] = ((Number) r.get(j)).intValue();
        }
        return m;
    }
}
