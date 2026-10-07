import java.util.*;

class Solution {
    public int[] getOrder(int[][] tasks) {
        int n = tasks.length;
        Integer[] byTime = new Integer[n];
        for (int i = 0; i < n; i++) byTime[i] = i;
        Arrays.sort(byTime, (a, b) -> Integer.compare(tasks[a][0], tasks[b][0]));
        PriorityQueue<Integer> ready = new PriorityQueue<>((a, b) ->
                tasks[a][1] != tasks[b][1] ? Integer.compare(tasks[a][1], tasks[b][1]) : Integer.compare(a, b));
        int[] order = new int[n];
        long time = 0;
        int next = 0, done = 0;
        while (done < n) {
            if (ready.isEmpty() && time < tasks[byTime[next]][0]) time = tasks[byTime[next]][0];
            while (next < n && tasks[byTime[next]][0] <= time) ready.add(byTime[next++]);
            int t = ready.poll();
            order[done++] = t;
            time += tasks[t][1];
        }
        return order;
    }
}
