import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        TicTacToe game = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "TicTacToe" -> { game = new TicTacToe(arg(a, 0)); out.add(null); }
                case "move" -> out.add(game.move(arg(a, 0), arg(a, 1), arg(a, 2)));
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }

    static int arg(List<?> a, int i) {
        return ((Number) a.get(i)).intValue();
    }
}
