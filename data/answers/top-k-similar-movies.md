**Short answer:** "Similar" is transitive, so the similar movies are the connected component containing `movie`. BFS from `movie` over the undirected similarity graph, and push every other movie in the component into a size-k min-heap ordered worst-first (lower rating, then larger label). Sort the k survivors best-first at the end. O(n + e + c log k), where c is the component size.

## Approach

- **Brute force.** BFS to collect the component, sort it by rating: O(n + e + c log c). Fine to state first.
- **Key insight.** Only k results are needed, so a size-k heap replaces the full sort: each candidate costs O(log k), and memory for ranking is O(k).
- **Graph.** Build an adjacency list with both directions of each pair. Use iterative BFS (or union-find) rather than recursive DFS, since a chain of 10⁵ movies would overflow the Java stack.
- **Exclude the query movie** itself from the heap.

## Solution

```java
import java.util.*;

class Solution {
    public int[] topSimilarMovies(int[] ratings, int[][] similar, int movie, int k) {
        int n = ratings.length;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : similar) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        // better = higher rating, then smaller label. The heap's head is the worst kept movie.
        Comparator<Integer> better = (a, b) -> ratings[a] != ratings[b]
                ? Integer.compare(ratings[b], ratings[a])
                : Integer.compare(a, b);
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
```

## Complexity

- **Time:** O(n + e) to build the graph and BFS, O(c log k) for the heap, O(k log k) for the final sort.
- **Space:** O(n + e) for the graph and `seen`; O(k) for the heap.

## Edge cases

- No similar movies: empty array.
- Fewer than k similar movies: return all, sorted.
- Equal ratings: the smaller label wins.
- Duplicate or repeated pairs: harmless thanks to `seen`.

## Follow-ups

- **Movies are added dynamically between queries.** Replace BFS with **union-find**: adding a similarity edge is a `union`, adding a movie is a new singleton. Each component root keeps its own top-k structure (a size-k min-heap or a sorted list). On `union`, merge the smaller component's top-k into the larger one's and trim back to k (O(k log k) per merge). A query reads the top-k of `find(movie)`, skipping the movie itself, so store k + 1 entries per component.
- **O(K) space and near O(log K) per update.** If the queries are always about one fixed movie, keep one size-K min-heap for its component. When a new movie joins that component (detected by `find`), offer it to the heap: O(log K), and the space for ranking stays O(K). When a whole other component merges in, offer its stored top-K entries: each offer is O(log K). Note that union-find itself needs O(n) for the parent array; the O(K) bound applies to the ranking state.
- **Ratings change:** a size-k heap cannot handle decreases; use a `TreeSet` ordered by (rating, label) per component.

Practise it in the app: Run / Submit on this page.
