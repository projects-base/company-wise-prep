import java.util.*;

class Solution {
    public int mostBooked(int n, int[][] meetings) {
        int[][] ms = meetings.clone();
        Arrays.sort(ms, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> free = new PriorityQueue<>();
        for (int i = 0; i < n; i++) free.add(i);
        // busy rooms: [endTime, room], earliest end first, then lowest room
        PriorityQueue<long[]> busy = new PriorityQueue<>((a, b) -> a[0] != b[0] ? Long.compare(a[0], b[0]) : Long.compare(a[1], b[1]));
        int[] count = new int[n];
        for (int[] m : ms) {
            long start = m[0], dur = m[1] - m[0];
            while (!busy.isEmpty() && busy.peek()[0] <= start) free.add((int) busy.poll()[1]);
            if (!free.isEmpty()) {
                int room = free.poll();
                count[room]++;
                busy.add(new long[] {start + dur, room});
            } else {
                long[] b = busy.poll();
                int room = (int) b[1];
                count[room]++;
                busy.add(new long[] {b[0] + dur, room});
            }
        }
        int best = 0;
        for (int i = 1; i < n; i++) if (count[i] > count[best]) best = i;
        return best;
    }
}
