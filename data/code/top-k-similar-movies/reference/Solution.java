import java.util.*;

class Solution {
    public int[] topSimilarMovies(int[] ratings, int[][] similar, int movie, int k) {
        int n = ratings.length;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : similar) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        // better = higher rating, then smaller label. The heap's head is the worst kept movie.
        Comparator<Integer> better = (a, b) -> ratings[a] != ratings[b] ? Integer.compare(ratings[b], ratings[a]) : Integer.compare(a, b);
        PriorityQueue<Integer> heap = new PriorityQueue<>(better.reversed());
        boolean[] seen = new boolean[n];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(movie);
        seen[movie] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            if (u != movie) {
                heap.add(u);
                if (heap.size() > k) heap.poll();
            }
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; q.add(v); }
        }
        List<Integer> out = new ArrayList<>(heap);
        out.sort(better);
        return out.stream().mapToInt(Integer::intValue).toArray();
    }
}
