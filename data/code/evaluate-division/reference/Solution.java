import java.util.*;

class Solution {
    // Graph: edge A -> B with weight k and B -> A with weight 1/k. A query is the product of the
    // weights along any path from C to D (consistency makes every path give the same answer).
    public double[] calcEquation(List<List<String>> equations, double[] values, List<List<String>> queries) {
        Map<String, Map<String, Double>> g = new HashMap<>();
        for (int i = 0; i < equations.size(); i++) {
            String a = equations.get(i).get(0), b = equations.get(i).get(1);
            g.computeIfAbsent(a, k -> new HashMap<>()).put(b, values[i]);
            g.computeIfAbsent(b, k -> new HashMap<>()).put(a, 1.0 / values[i]);
        }
        double[] out = new double[queries.size()];
        for (int q = 0; q < queries.size(); q++) {
            String c = queries.get(q).get(0), d = queries.get(q).get(1);
            out[q] = (g.containsKey(c) && g.containsKey(d)) ? search(g, c, d) : -1.0;
        }
        return out;
    }

    // Iterative DFS carrying the product of weights from the start.
    private static double search(Map<String, Map<String, Double>> g, String from, String to) {
        Deque<Object[]> stack = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        stack.push(new Object[] {from, 1.0});
        seen.add(from);
        while (!stack.isEmpty()) {
            Object[] top = stack.pop();
            String u = (String) top[0];
            double acc = (Double) top[1];
            if (u.equals(to)) return acc;
            for (Map.Entry<String, Double> e : g.get(u).entrySet()) {
                if (seen.add(e.getKey())) stack.push(new Object[] {e.getKey(), acc * e.getValue()});
            }
        }
        return -1.0;
    }
}
