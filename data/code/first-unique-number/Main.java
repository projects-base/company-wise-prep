import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        List<?> ops = in.nextList();
        List<?> params = in.nextList();
        List<Object> out = new ArrayList<>();
        FirstUnique fu = null;
        for (int i = 0; i < ops.size(); i++) {
            String op = (String) ops.get(i);
            List<?> a = (List<?>) params.get(i);
            switch (op) {
                case "FirstUnique" -> {
                    List<?> l = (List<?>) a.get(0);
                    int[] nums = new int[l.size()];
                    for (int j = 0; j < nums.length; j++) nums[j] = ((Number) l.get(j)).intValue();
                    fu = new FirstUnique(nums);
                    out.add(null);
                }
                case "showFirstUnique" -> out.add(fu.showFirstUnique());
                case "add" -> { fu.add(((Number) a.get(0)).intValue()); out.add(null); }
                default -> throw new IllegalArgumentException("Unknown operation " + op);
            }
        }
        IO.print(out);
    }
}
