import java.util.*;

class Solution {
    public int minMeetingRooms(int[][] intervals) {
        int n = intervals.length;
        int[] starts = new int[n], ends = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i] = intervals[i][1];
        }
        Arrays.sort(starts);
        Arrays.sort(ends);
        int rooms = 0, best = 0, j = 0;
        for (int i = 0; i < n; i++) {
            // free every room whose meeting ended at or before this start
            while (ends[j] <= starts[i]) { j++; rooms--; }
            rooms++;
            best = Math.max(best, rooms);
        }
        return best;
    }
}
