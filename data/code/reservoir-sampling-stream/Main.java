import java.util.*;

/*
 * Statistical harness. Input: stream (list, or N meaning 0..N-1), checkpoints (prefix lengths),
 * trials, seed. Prints one verdict per checkpoint.
 */
public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        Object streamSpec = in.nextValue();
        int[] checkpoints = in.nextIntArray();
        int trials = in.nextInt();
        long seed = in.nextLong();

        int[] stream = null;
        long length;
        if (streamSpec instanceof List<?> l) {
            stream = new int[l.size()];
            for (int i = 0; i < stream.length; i++) stream[i] = ((Number) l.get(i)).intValue();
            length = stream.length;
        } else {
            length = ((Number) streamSpec).longValue();
        }

        int cps = checkpoints.length;
        List<Map<Integer, Integer>> observed = new ArrayList<>();
        for (int c = 0; c < cps; c++) observed.add(new HashMap<>());
        Random rng = new Random(seed);
        for (int t = 0; t < trials; t++) {
            StreamSampler s = new StreamSampler(rng);
            int c = 0;
            for (long i = 0; i < length && c < cps; i++) {
                s.add(stream != null ? stream[(int) i] : (int) i);
                while (c < cps && checkpoints[c] == i + 1) {
                    observed.get(c).merge(s.sample(), 1, Integer::sum);
                    c++;
                }
            }
        }

        List<String> verdicts = new ArrayList<>();
        for (int c = 0; c < cps; c++) verdicts.add(judge(stream, checkpoints[c], trials, observed.get(c)));
        IO.print(verdicts);
    }

    // expected multiplicity of each value among the first p elements
    static String judge(int[] stream, int p, int trials, Map<Integer, Integer> obs) {
        Map<Integer, Integer> mult = new HashMap<>();
        if (stream != null) {
            for (int i = 0; i < p; i++) mult.merge(stream[i], 1, Integer::sum);
        }
        for (int v : obs.keySet()) {
            boolean member = stream != null ? mult.containsKey(v) : (v >= 0 && v < p);
            if (!member) return "invalid: returned " + v + ", which is not among the first " + p + " elements";
        }
        int distinct = stream != null ? mult.size() : p;
        if (distinct <= 1) return "uniform";
        if (stream == null || trials < 5 * distinct) return "valid";
        double chi2 = 0;
        int worstValue = 0;
        double worstDiff = -1, worstExp = 0;
        for (Map.Entry<Integer, Integer> e : mult.entrySet()) {
            double expect = (double) trials * e.getValue() / p;
            int o = obs.getOrDefault(e.getKey(), 0);
            chi2 += (o - expect) * (o - expect) / expect;
            double diff = Math.abs(o - expect) / Math.sqrt(expect);
            if (diff > worstDiff) {
                worstDiff = diff;
                worstValue = e.getKey();
                worstExp = expect;
            }
        }
        int df = distinct - 1;
        // Wilson-Hilferty: chi2/df is roughly normal after a cube root
        double z = (Math.cbrt(chi2 / df) - (1 - 2.0 / (9 * df))) / Math.sqrt(2.0 / (9 * df));
        if (z > 6) {
            return String.format(Locale.ROOT, "biased: value %d was returned %d times, expected about %.0f",
                    worstValue, obs.getOrDefault(worstValue, 0), worstExp);
        }
        return "uniform";
    }
}
