import java.util.*;

class Solution {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> next = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) next.add(new ArrayList<>());
        int[] indeg = new int[numCourses];
        for (int[] p : prerequisites) {
            next.get(p[1]).add(p[0]);
            indeg[p[0]]++;
        }
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) if (indeg[i] == 0) q.add(i);
        int[] order = new int[numCourses];
        int k = 0;
        while (!q.isEmpty()) {
            int c = q.poll();
            order[k++] = c;
            for (int d : next.get(c)) if (--indeg[d] == 0) q.add(d);
        }
        return k == numCourses ? order : new int[0];
    }
}
