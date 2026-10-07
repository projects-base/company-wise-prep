import java.util.*;

class Solution {
    private List<Integer>[] adj;
    private int[] volunteerOf, questionOf, dist;

    @SuppressWarnings("unchecked")
    public int maxAssignments(String[][] questions, String[][] volunteers) {
        int q = questions.length, v = volunteers.length;
        // tag -> volunteers having it
        Map<String, List<Integer>> byTag = new HashMap<>();
        for (int j = 0; j < v; j++) for (String t : volunteers[j]) byTag.computeIfAbsent(t, k -> new ArrayList<>()).add(j);
        adj = new List[q];
        for (int i = 0; i < q; i++) {
            Set<Integer> can = new LinkedHashSet<>();
            for (String t : questions[i]) can.addAll(byTag.getOrDefault(t, List.of()));
            adj[i] = new ArrayList<>(can);
        }
        // Hopcroft-Karp
        volunteerOf = new int[q];
        questionOf = new int[v];
        dist = new int[q];
        Arrays.fill(volunteerOf, -1);
        Arrays.fill(questionOf, -1);
        int matched = 0;
        while (bfs(q)) {
            for (int i = 0; i < q; i++) if (volunteerOf[i] == -1 && dfs(i)) matched++;
        }
        return matched;
    }

    private boolean bfs(int q) {
        Deque<Integer> queue = new ArrayDeque<>();
        boolean found = false;
        for (int i = 0; i < q; i++) {
            if (volunteerOf[i] == -1) {
                dist[i] = 0;
                queue.add(i);
            } else dist[i] = -1;
        }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            for (int j : adj[i]) {
                int other = questionOf[j];
                if (other == -1) found = true;
                else if (dist[other] == -1) {
                    dist[other] = dist[i] + 1;
                    queue.add(other);
                }
            }
        }
        return found;
    }

    private boolean dfs(int i) {
        for (int j : adj[i]) {
            int other = questionOf[j];
            if (other == -1 || (dist[other] == dist[i] + 1 && dfs(other))) {
                volunteerOf[i] = j;
                questionOf[j] = i;
                return true;
            }
        }
        dist[i] = -1;
        return false;
    }
}
