import java.util.*;

class Solution {
    public List<Segment> schedule(String[] names, int[][] times) {
        // Events: +1 at a shift's start, -1 at its end, grouped by time.
        TreeMap<Integer, List<int[]>> events = new TreeMap<>(); // time -> list of {shift, delta}
        for (int i = 0; i < names.length; i++) {
            events.computeIfAbsent(times[i][0], k -> new ArrayList<>()).add(new int[] {i, 1});
            events.computeIfAbsent(times[i][1], k -> new ArrayList<>()).add(new int[] {i, -1});
        }
        Map<String, Integer> active = new HashMap<>(); // worker -> number of their shifts covering now
        TreeSet<String> onDuty = new TreeSet<>();
        List<Segment> out = new ArrayList<>();
        int segStart = -1;
        for (Map.Entry<Integer, List<int[]>> e : events.entrySet()) {
            int t = e.getKey();
            Set<String> touched = new HashSet<>();
            Map<String, Boolean> before = new HashMap<>();
            for (int[] ev : e.getValue()) {
                String w = names[ev[0]];
                if (touched.add(w)) before.put(w, active.getOrDefault(w, 0) > 0);
                active.merge(w, ev[1], Integer::sum);
            }
            boolean changed = false;
            for (String w : touched) {
                boolean now = active.get(w) > 0;
                if (now != before.get(w)) changed = true;
            }
            if (!changed) continue;
            if (!onDuty.isEmpty()) out.add(new Segment(segStart, t, new ArrayList<>(onDuty)));
            for (String w : touched) {
                if (active.get(w) > 0) onDuty.add(w); else onDuty.remove(w);
            }
            segStart = t;
        }
        return out;
    }
}
