import java.util.*;

class Solution {
    public int[][] freeRanges(int people, int totalDays, int[][] busy, int minFree) {
        // 1. Merge each person's blocks so overlapping blocks are not double-counted.
        int[][] blocks = busy.clone();
        Arrays.sort(blocks, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        TreeMap<Long, Integer> delta = new TreeMap<>(); // day -> change in busy-people count
        int i = 0;
        while (i < blocks.length) {
            int person = blocks[i][0];
            long s = blocks[i][1], e = blocks[i][2];
            i++;
            while (i < blocks.length && blocks[i][0] == person && blocks[i][1] <= e + 1) {
                e = Math.max(e, blocks[i][2]);
                i++;
            }
            delta.merge(s, 1, Integer::sum);
            delta.merge(e + 1, -1, Integer::sum);
        }
        delta.putIfAbsent(1L, 0);
        delta.putIfAbsent((long) totalDays + 1, 0);

        // 2. Sweep: between consecutive change points the busy count is constant.
        List<int[]> out = new ArrayList<>();
        int busyNow = 0;
        Long prev = null;
        for (Map.Entry<Long, Integer> en : delta.entrySet()) {
            long day = en.getKey();
            if (prev != null && prev <= totalDays && people - busyNow >= minFree) {
                int from = (int) (long) prev, to = (int) Math.min(day - 1, totalDays);
                if (!out.isEmpty() && out.get(out.size() - 1)[1] == from - 1) out.get(out.size() - 1)[1] = to;
                else out.add(new int[] {from, to});
            }
            busyNow += en.getValue();
            prev = day;
        }
        return out.toArray(new int[0][]);
    }
}
