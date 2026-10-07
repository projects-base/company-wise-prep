import java.util.*;

class Solution {
    // A pattern has 2^4 possible wildcard layouts. For a query, build the 16 strings obtained by
    // replacing each subset of its parts with '*' and look each one up in a set of the patterns.
    public boolean[] matchIps(String[] patterns, String[] queries) {
        Set<String> set = new HashSet<>(Arrays.asList(patterns));
        boolean[] out = new boolean[queries.length];
        for (int q = 0; q < queries.length; q++) {
            String[] parts = queries[q].split("[.]");
            for (int mask = 0; mask < 16 && !out[q]; mask++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < 4; k++) {
                    if (k > 0) sb.append('.');
                    sb.append((mask >> k & 1) == 1 ? "*" : parts[k]);
                }
                out[q] = set.contains(sb.toString());
            }
        }
        return out;
    }
}
