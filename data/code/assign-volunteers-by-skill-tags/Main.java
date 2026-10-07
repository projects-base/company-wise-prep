import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[][] questions = in.nextStringMatrix();
        String[][] volunteers = in.nextStringMatrix();
        IO.print(new Solution().maxAssignments(questions, volunteers));
    }
}
