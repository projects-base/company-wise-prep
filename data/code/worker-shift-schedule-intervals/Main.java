import java.util.*;

class Segment {
    int start, end;
    List<String> workers;

    Segment(int start, int end, List<String> workers) {
        this.start = start;
        this.end = end;
        this.workers = workers;
    }
}

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String[] names = in.nextStringArray();
        int[][] times = in.nextIntMatrix();
        List<Segment> result = new Solution().schedule(names, times);
        List<Object> out = new ArrayList<>();
        if (result != null) {
            for (Segment s : result) {
                // Names may come in any order, so sort them for a single canonical output.
                List<String> w = s.workers == null ? new ArrayList<>() : new ArrayList<>(s.workers);
                Collections.sort(w);
                out.add(List.of(s.start, s.end, w));
            }
        }
        IO.print(out);
    }
}
