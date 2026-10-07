import java.util.*;

class Solution {
    public int[][][] externalSort(int[] file, int memory) {
        List<int[][]> stages = new ArrayList<>();

        // Stage 0: sorted runs of at most `memory` numbers.
        List<int[]> runs = new ArrayList<>();
        for (int start = 0; start < file.length; start += memory) {
            int[] chunk = Arrays.copyOfRange(file, start, Math.min(file.length, start + memory));
            Arrays.sort(chunk);
            runs.add(chunk);
        }
        stages.add(runs.toArray(new int[0][]));

        // Merge passes with fan-in memory - 1.
        int fanIn = memory - 1;
        while (runs.size() > 1) {
            List<int[]> next = new ArrayList<>();
            for (int g = 0; g < runs.size(); g += fanIn) {
                next.add(mergeGroup(runs.subList(g, Math.min(runs.size(), g + fanIn))));
            }
            runs = next;
            stages.add(runs.toArray(new int[0][]));
        }
        return stages.toArray(new int[0][][]);
    }

    /** k-way merge: a min-heap holds one cursor (run, position) per run. */
    private int[] mergeGroup(List<int[]> group) {
        int total = 0;
        for (int[] r : group) total += r.length;
        int[] out = new int[total];
        PriorityQueue<int[]> heap = new PriorityQueue<>((x, y) -> Integer.compare(group.get(x[0])[x[1]], group.get(y[0])[y[1]]));
        for (int r = 0; r < group.size(); r++) if (group.get(r).length > 0) heap.add(new int[] {r, 0});
        int k = 0;
        while (!heap.isEmpty()) {
            int[] cur = heap.poll();
            int[] run = group.get(cur[0]);
            out[k++] = run[cur[1]];
            if (cur[1] + 1 < run.length) heap.add(new int[] {cur[0], cur[1] + 1});
        }
        return out;
    }
}
