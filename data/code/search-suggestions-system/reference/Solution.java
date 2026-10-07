import java.util.*;

class Solution {
    public List<List<String>> suggestedProducts(String[] products, String searchWord) {
        String[] sorted = products.clone();
        Arrays.sort(sorted);
        List<List<String>> out = new ArrayList<>();
        StringBuilder prefix = new StringBuilder();
        int from = 0;
        for (char c : searchWord.toCharArray()) {
            prefix.append(c);
            String p = prefix.toString();
            from = lowerBound(sorted, p, from);
            List<String> step = new ArrayList<>();
            for (int i = from; i < sorted.length && step.size() < 3 && sorted[i].startsWith(p); i++) step.add(sorted[i]);
            out.add(step);
        }
        return out;
    }

    /** First index >= lo whose word is >= key. */
    private int lowerBound(String[] a, String key, int lo) {
        int hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid].compareTo(key) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
